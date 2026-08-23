#---------------------------------------------------
# file: cllm.py
# desc: represents the llm configuration
#---------------------------------------------------
import json
import re
import os
import logging
import urllib.error
import urllib.request
from c3dclasses.ccore.cutility.cutility import extractTextFromFilename, writeTextToFilename

#-----------------------------------------------
# Initialize logging
#-----------------------------------------------
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
if not logger.handlers:
    handler = logging.StreamHandler()
    handler.setLevel(logging.INFO)
    handler.setFormatter(logging.Formatter("%(levelname)s:%(name)s:%(message)s"))
    logger.addHandler(handler)
logger.propagate = False


class CSimpleOllama:
    def __init__(self, model="llama3.1", temperature=0.4, top_p=0.40, stop=None, api_base="http://localhost:11434"):
        self.model = model
        self.temperature = temperature
        self.top_p = top_p
        self.stop = stop
        self.api_base = api_base.rstrip("/")

    def _invoke_at_base(self, prompt, api_base):
        payload = {
            "model": self.model,
            "prompt": prompt,
            "stream": False,
            "options": {
                "temperature": self.temperature,
                "top_p": self.top_p
            }
        }
        if self.stop:
            payload["options"]["stop"] = self.stop

        data = json.dumps(payload).encode("utf-8")
        request = urllib.request.Request(
            api_base + "/api/generate",
            data=data,
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(request, timeout=300) as response:
            result = json.loads(response.read().decode("utf-8"))
            return result.get("response", "")

    def _alternate_api_bases(self):
        if self.api_base in ("http://localhost:11434", "http://127.0.0.1:11434"):
            return ["http://[::1]:11434"]
        return []

    def invoke(self, prompt):
        try:
            return self._invoke_at_base(prompt, self.api_base)
        except urllib.error.HTTPError as e:
            error_body = e.read().decode("utf-8", errors="replace")
            if e.code == 404 and "model" in error_body and "not found" in error_body:
                for api_base in self._alternate_api_bases():
                    try:
                        return self._invoke_at_base(prompt, api_base)
                    except urllib.error.URLError:
                        pass
                # end for
            # end if
            raise RuntimeError("Unable to reach Ollama at " + self.api_base + ". Start Ollama and pull model " + self.model + ".") from e
        except urllib.error.URLError as e:
            raise RuntimeError("Unable to reach Ollama at " + self.api_base + ". Start Ollama and pull model " + self.model + ".") from e

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
        from langchain.chains.question_answering import load_qa_chain
        from langchain_community.callbacks.manager import get_openai_callback

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
        from langchain_community.chat_models import ChatOpenAI

        return ChatOpenAI (
            model=self.model,  
            temperature=self.temperature,  
            max_tokens=self.max_tokens
        )
    # end _init_chat_openai_platform()
    
    def _get_openai_platform(self):
        from langchain_community.llms.openai import OpenAI

        return OpenAI (
            model=self.model,   # Specify the non-chat model
            temperature=self.temperature,  
            max_tokens=self.max_tokens
        )
    # end _init_openai_platform()
    
    def _get_ollama_platform(self):
        try:
            from langchain_community.llms.ollama import Ollama

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
        except ModuleNotFoundError as e:
            if e.name != "langchain_community":
                raise
            api_base = self.api_base.replace("/v1", "")
            return CSimpleOllama(
                model=self.model,
                temperature=self.temperature,
                top_p=self.top_p,
                stop=self.stop,
                api_base=api_base
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
        super().__init__() 
    # end __init__()    
    
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
    
    def prompt(self, strprompt): 
        self.m_strpromptresponse = self._prompt(strprompt)
        return self.m_promptresponse
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
