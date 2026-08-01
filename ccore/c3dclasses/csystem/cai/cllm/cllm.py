#---------------------------------------------------
# file: cllm.py
# desc: represents the llm configuration
#---------------------------------------------------
import json
import re
import os
import logging
import requests
from pathlib import Path
import ollama
from c3dclasses.ccore.cutility.cutility import extractTextFromFilename, writeTextToFilename
from langchain_community.llms.ollama import Ollama
from langchain_community.llms.openai import OpenAI
from langchain.chains.question_answering import load_qa_chain
from langchain_community.callbacks.manager import get_openai_callback
from langchain.output_parsers import RetryWithErrorOutputParser
from langchain_community.chat_models.openai import ChatOpenAI
from langchain.schema import HumanMessage, SystemMessage

#-----------------------------------------------
# Initialize logging
#-----------------------------------------------
logging.basicConfig(level=logging.WARNING)
logger = logging.getLogger(__name__)

#------------------------------------------------------
# name: CLLMSettings
# desc: stores the LLM settings for prompt calls
#------------------------------------------------------
class CLLMSettings:
    def __init__(self, 
                 cllm=None,
                 max_tokens=8500, 
                 temperature=0.4, 
                 top_p=0.40,
                 top_k=50,
                 best_of=3,
                 frequency_penalty=0.5,
                 presence_penalty=0.0,
                 n=1, 
                 echo=False, 
                 stream=False,
                 api_base="http://localhost:11434/v1", 
                 api_key="not needed for a local LLM", 
                 model="llama3.1",
                 model_platform="Ollama",
                 format=None,
                 chain_type="stuff", 
                 format4=None,
                 stop=None
                 ):
        self.max_tokens = max_tokens
        self.temperature = temperature
        self.top_p = top_p
        self.top_k = top_k
        self.n = n
        self.echo = echo
        self.best_of = best_of
        self.frequency_penalty = frequency_penalty
        self.presence_penalty = presence_penalty
        self.stream = stream        
        self.api_base = api_base
        self.api_key = api_key
        self.model = model
        self.cllm = cllm
        self.format = format
        self.retry = 1
        self.chain_type = chain_type
        self.llm = None
        self.model_platform = model_platform
        self.stop = stop
        #self.setOllama()
    # end __init__()
    
    #---------------------------------------------------------------
    # selecting platform and model
    #--------------------------------------------------------------- 
    def useChatOpenAI(self, model="gpt-3.5-turbo"):
        self.model = model
        self.model_platform = "ChatOpenAI"
        self.max_tokens = 1000
    # end useChatOpenAI()
    
    def useOllama(self, model="llama3.1"):
        self.model = model
        self.model_platform = "Ollama"
        self.max_tokens = 8000
    # end useOllama()
    
    def useOpenAI(self, model="gpt-3.5-turbo-instruct"):
        self.model = model
        self.model_platform = "OpenAI"
        self.max_tokens = 1000
    # end useOpenAI()
    
    #--------------------------------------------------------------
    # parameters
    #--------------------------------------------------------------
    def setMaxTokens(self, imaxtokens):
        self.max_tokens = imaxtokens
        return self
    # end setMaxTokens()

    def getMaxTokens(self):
        return self.max_tokens
    # end getMaxTokens()

    def setTemperature(self, temperature):
        self.temperature = temperature
        return self
    # end setTemperature()
    
    def getTemperature(self):
        return self.temperature
    # end getTemperature()
        
    def setModel(self, model):
        self.model = model
        return self
    # end setModel()
    
    def getModel(self):
        return self.model
    # end getModel()
    
    def setTopP(self, top_p):
        self.top_p = top_p
        return self
    # end setMaxTokens()

    def getTopP(self):
        return self.top_p
    # end getTopP()

    def setTopK(self, top_k):
        self.top_k = top_k
        return self
    # end setTopK()

    def getTopK(self):
        return self.top_k
    # end getTopK()

    def setFrequencyPenalty(self, frequency_penalty):
        self.frequency_penalty = frequency_penalty
        return self
    # end setMaxTokens()

    def getFrequencyPenalty(self):
        return self.frequency_penalty
    # end getFrequencyPenalty()

    def setPresencePenalty(self, presence_penalty):
        self.presence_penalty = presence_penalty
        return self
    # end getPresencePenalty()

    def getPresencePenalty(self):
        return self.presence_penalty
    # end getPresencePenalty()
    
    def setStop(self, stop):
        self.stop = stop
        return self
    # end setStop()
    
    def getStop(self):
        return self.stop
    # end getStop()
    
    #---------------------------------------------------------------
    # prompting methods
    #---------------------------------------------------------------
    def _prompt(self, strprompt):
        llm = self._getLLM()
        return llm.invoke(strprompt) 
    # end prompt()
        
    def _chain(self, strquestion, docs):
        llm = self._getLLM()
        chain = load_qa_chain(llm, chain_type=self.chain_type)
        with get_openai_callback() as cb:    
            strresponse = chain.invoke( {"input_documents":docs, "question": strquestion} )
            return strresponse["output_text"]
        # end with
    # end _chain()

    
    #--------------------------------------------------------------------------
    # helper functions for initializing the llm chat/non chat platforms
    #--------------------------------------------------------------------------
    def _getLLM(self, params=None):   
        if(self.model_platform == "ChatOpenAI"):
            return self._get_chat_openai_platform()
        # end if
        elif(self.model_platform == "OpenAI"):
            return self._get_openai_platform()
        # end if
        elif(self.model_platform == "Ollama"):
            return self._get_ollama_platform()
        # end if
        return None          
    # end getLLM()  
    
    def _get_chat_openai_platform(self):
        return ChatOpenAI (
            model=self.model,  
            temperature=self.temperature,  
            max_tokens=self.max_tokens
        )
    # end _init_chat_openai_platform()
    
    def _get_openai_platform(self):
        return OpenAI (
            model=self.model,   # Specify the non-chat model
            temperature=self.temperature,  
            max_tokens=self.max_tokens
        )
    # end _init_openai_platform()
    
    def _get_ollama_platform(self):
        return Ollama (
            model=self.model,
            temperature=self.temperature,
            #max_tokens=self.max_tokens,
            top_p=self.top_p,
            #top_k=self.top_k,
            #frequency_penalty=self.frequency_penalty,
            #presence_penalty=self.presence_penalty,
            stop=self.stop
        )      
    # end _init_chat_ollama_plafrom()
    
# end CLLSettings
  
#---------------------------------------------------------------------
# name: CLLM
# desc: define an object that operates on a large language model
#---------------------------------------------------------------------
class CLLM (CLLMSettings): 
    def __init__(self):
        self.m_strprompt = ""
        self.m_strpromptresponse = ""
        self.m_promptresponse = None
        self.m_repromptresponse = None
        self.m_memoryon = False          # when True, past turns are remembered
        self.m_systemprompt = ""         # optional persistent system instruction
        self.m_history = []              # list of (role, text) conversation turns
        super().__init__() 
    # end __init__()    

    #-----------------------------------------------------
    # conversation memory methods
    #-----------------------------------------------------
    def enableMemory(self, systemprompt=""):
        # turn on conversation memory so the model remembers prior turns
        self.m_memoryon = True
        self.m_systemprompt = systemprompt
        # stop the model from hallucinating the user's next turn
        self.setStop(["\nUser:", "\nSystem:"])
        return self
    # end enableMemory()

    def disableMemory(self):
        self.m_memoryon = False
        return self
    # end disableMemory()

    def clearMemory(self):
        # forget all prior turns but keep the system prompt
        self.m_history = []
        return self
    # end clearMemory()

    def getHistory(self):
        return self.m_history
    # end getHistory()

    def getHistorySize(self):
        # total token size of the conversation history context
        size = 0
        for role, text in self.m_history:
            size += self.countTokens(f"{role}: {text}")
        return size
    # end getHistorySize()

    def getSystemPromptSize(self):
        # total token size of the system prompt context
        if self.m_systemprompt:
            return self.countTokens(f"System: {self.m_systemprompt}")
        return 0
    # end getSystemPromptSize()

    def getContextSize(self):
        # total token size of the full conversation context
        return self.getSystemPromptSize() + self.getHistorySize()
    # end getContextSize()

    def getRemainingContextSize(self):
        # remaining input token budget: the model's window minus the current
        # context and minus the tokens reserved for the generated response
        return self.getModelContextWindow() - self.getContextSize() - self.getMaxTokens()
    # end getRemainingContextSize()

    def countTokens(self, text):
        # count the number of tokens in text using the Ollama tokenizer
        if not text:
            return 0
        # Ollama has no /api/tokenize endpoint; ask /api/generate to evaluate the
        # prompt without generating (num_predict=0) and read prompt_eval_count.
        # Disable further attempts once the server proves it can't provide counts.
        if self.model_platform == "Ollama" and getattr(self, "m_cantokenize", True):
            try:
                host = self.api_base.rsplit("/v1", 1)[0] if self.api_base else "http://localhost:11434"
                response = requests.post(
                    f"{host}/api/generate",
                    json={
                        "model": self.model,
                        "prompt": text,
                        "stream": False,
                        "options": {"num_predict": 0},
                    },
                    timeout=30
                )
                response.raise_for_status()
                count = response.json().get("prompt_eval_count")
                if count is not None:
                    return int(count)
            except Exception as e:
                # remember the failure so we stop hitting the server every call
                self.m_cantokenize = False
                logger.warning(f"Failure: CLLM :: countTokens() - falling back to estimate ({e}).")
        # fallback: rough estimate of ~4 characters per token
        return (len(text) + 3) // 4
    # end countTokens()

    def getModelContextWindow(self, default=8192):
        # query the running Ollama server for the model's real context length (in tokens)
        if self.model_platform != "Ollama":
            logger.warning("Failure: CLLM :: getModelContextWindow() - only supported for the Ollama platform.")
            return default
        try:
            # derive the server host from the configured api_base (strip the /v1 suffix)
            host = self.api_base.rsplit("/v1", 1)[0] if self.api_base else "http://localhost:11434"
            response = requests.post(f"{host}/api/show", json={"name": self.model}, timeout=10)
            response.raise_for_status()
            info = response.json().get("model_info", {})
            # the key is architecture-specific, e.g. "llama.context_length"
            for key, value in info.items():
                if key.endswith("context_length"):
                    return int(value)
            logger.warning("Failure: CLLM :: getModelContextWindow() - context_length not found in model info.")
        except Exception as e:
            logger.error(f"Failure: CLLM :: getModelContextWindow() - Error querying Ollama: {e}")
        return default
    # end getModelContextWindow()

    def _buildConversation(self, strprompt):
        # assemble the full conversation transcript for a memory-enabled prompt
        parts = []
        if self.m_systemprompt:
            parts.append(f"System: {self.m_systemprompt}")
        for role, text in self.m_history:
            parts.append(f"{role}: {text}")
        parts.append(f"User: {strprompt}")
        parts.append("Assistant:")
        return "\n".join(parts)
    # end _buildConversation()
    
    #-----------------------------------------------------
    # generating methods of prompts or text from prompts
    #-----------------------------------------------------
    def generatePromptFromText(self, strinfotogeneratefrom, choice=0): 
        strprompt = f"Please generate an accurate and well-structured prompt based on the following information:\n{strinfotogeneratefrom}\n"
        response = self.prompt(strinfotogeneratefrom)
        response = self.getPromptResponse()
        if response:
            return response 
        logger.warning("Failure: CLLM :: generatePromptFromText() - No response returned from _prompt_text.")
        return None 
    # end generatePromptFromText()
    
    def generateTextFromPrompt(self, strprompt, strcachefilename=""):
        # retrieve the text from cache resources
        if(strcachefilename and os.path.exists(strcachefilename)):
            strtext = extractTextFromFilename(strcachefilename)
        # end if
        else:
            # generate the text and save it to a filename
            strtext = self.prompt(strprompt)
            strtext = self.getPromptResponse()
            if(strcachefilename):
                writeTextToFilename(strcachefilename, strtext)
            # end if
        # end else 
        self.m_strpromptresponse = strtext
        return strtext
    # end generateTextFromPrompt()
           
    def getPrompt(self): 
        return self.m_strprompt
    # end getPrompt()

    # prompt response
    def setPromptResponse(self, strresponse):
        self.m_strpromptresponse = strresponse
    # end getPromptResponse()
    
    # prompt response
    def getPromptResponse(self):
        return self.m_strpromptresponse
    # end getPromptResponse()

    def _promptWithImages(self, strprompt, strpathimages):
        if self.model_platform != "Ollama":
            raise ValueError("Image prompts are only supported on the Ollama platform.")

        if isinstance(strpathimages, (str, Path)):
            image_paths = [strpathimages]
        elif isinstance(strpathimages, (list, tuple)):
            image_paths = list(strpathimages)
        else:
            raise TypeError("strpathimages must be a path string, Path, or list/tuple of paths.")

        normalized_paths = []
        for image_path in image_paths:
            path = Path(image_path)
            if not path.exists():
                raise FileNotFoundError(f"Image not found: {path}")
            normalized_paths.append(str(path))

        host = self.api_base.rsplit("/v1", 1)[0] if self.api_base else "http://localhost:11434"
        client = ollama.Client(host=host)

        messages = []
        if self.m_memoryon and self.m_systemprompt:
            messages.append({"role": "system", "content": self.m_systemprompt})
        if self.m_memoryon:
            for role, text in self.m_history:
                role_name = role.lower()
                if role_name not in ("user", "assistant", "system"):
                    role_name = "user"
                messages.append({"role": role_name, "content": text})

        messages.append(
            {
                "role": "user",
                "content": strprompt,
                "images": normalized_paths,
            }
        )

        options = {
            "temperature": self.temperature,
            "top_p": self.top_p,
            "top_k": self.top_k,
            "num_ctx": self.getModelContextWindow(),
        }
        if self.stop:
            options["stop"] = self.stop

        response = client.chat(
            model=self.model,
            messages=messages,
            options=options,
        )

        return response.get("message", {}).get("content", "")
    # end _promptWithImages()
    
    def prompt(self, strprompt, strpathimages=None): 
        self.m_strprompt = strprompt
        if strpathimages:
            strresponse = self._promptWithImages(strprompt, strpathimages)
            strresponse = strresponse.strip() if strresponse else strresponse
            if self.m_memoryon:
                self.m_history.append(("User", strprompt))
                self.m_history.append(("Assistant", strresponse))
            self.m_strpromptresponse = strresponse
        else:
            if self.m_memoryon:
                # send the full transcript so the model remembers the conversation
                strresponse = self._prompt(self._buildConversation(strprompt))
                strresponse = strresponse.strip() if strresponse else strresponse
                # record this turn so it is remembered on the next call
                self.m_history.append(("User", strprompt))
                self.m_history.append(("Assistant", strresponse))
                self.m_strpromptresponse = strresponse
            else:
                self.m_strpromptresponse = self._prompt(strprompt)
        return self.m_strpromptresponse
    # end prompt()


    
    def chain(self,  strquestion, docs):
        self.m_strpromptresponse = super().chain(strquestion, docs)
        return self.m_promptresponse
    # end chain()
       
    #--------------------------------------------------------------
    # parsing methods
    #--------------------------------------------------------------    
    def parseAllCode(self):
        logger.info("Success: CLLM :: parseAllCode() - Parsing all code blocks from prompt response.")
        if self.m_strpromptresponse:
            code_pattern = r'```(.*?)```'
            code_matches = re.findall(code_pattern, self.m_strpromptresponse, re.DOTALL)
            logger.debug(f"Success: CLLM :: parseAllCode() - Code blocks found: {code_matches}")
            return [code_block.strip() for code_block in code_matches]
        return None    
    # end parseAllCode()
    
    def parseCode(self, strtype):
        logger.info(f"Success: CLLM :: parseCode() - Parsing specific code of type: {strtype}")
        allcode = self.parseAllCode()
        if allcode:
            for code in allcode: 
                if strtype in code:
                    return code.replace(strtype, "").strip()
            return allcode[0].strip()
        return "" 
    # end parseCode()
 
    def parseJSON(self):
        try:
            return json.loads(self.parseCode("json"))
        except json.JSONDecodeError as e:
            logger.error(f"Failure: CLLM :: parseJSON() - Error parsing JSON: {e}")
            return None
    # end parseJSON()
    
    
    def summarize(self, text, strconstraints, max_chunk_size=3000, summary_length=150):
        """
        Summarize a large body of text using an LLM.

        Parameters:
            text (str): The large text to summarize.
            model (str): The model to use for summarization.
            max_chunk_size (int): Maximum character limit per chunk.
            summary_length (int): Approximate length of each summary in words.

        Returns:
            str: A cohesive summary of the text.
        """
        # Split the text into manageable chunks
        chunks = []
        while len(text) > max_chunk_size:
            # Split at the last sentence within the chunk size limit
            split_point = text[:max_chunk_size].rfind(". ")
            if split_point == -1:
                split_point = max_chunk_size
            chunks.append(text[:split_point + 1])
            text = text[split_point + 1:]
        chunks.append(text)

        # Summarize each chunk
        summaries = []
        for i, chunk in enumerate(chunks):
            print(f"Success: CLLM :: summarize() - Processing chunk {i + 1} of {len(chunks)}...")
            prompt = (
                f"Summarize the following java code in approximately {summary_length} words.\n\nContraints of Summary:\n{strconstraints}\n\nChunk of Text to Summerize\n{chunk}"
            )
            try:
                summary = self._prompt(prompt)
                summaries.append(summary.strip())
            except Exception as e:
                print(f"Failure: CLLM :: summarize() - Error summarizing chunk {i + 1}: {e}")
                summaries.append("")
        # end fof
        
        # Combine the chunk summaries
        final_summary_prompt = (
            "Combine the following summaries into a cohesive overall summary:\n\n"
            + "\n\n".join(summaries)
        )
        try:
            self.setMaxTokens(summary_length * 4),
            final_summary = self._prompt(final_summary_prompt)
        except Exception as e:
            print(f"Failure: CLLM :: summarize() - Error generating final summary: {e}")
            final_summary = " ".join(summaries)  # Fallback to concatenated summaries
        return final_summary
    # end summarize_large_text()    
# end CLLM


