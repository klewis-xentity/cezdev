#---------------------------------------------------
# file: cllm.py
# desc: represents the llm configuration
#---------------------------------------------------
import math
import json
import re
import os
import logging
import requests
from pathlib import Path
import ollama
from c3dclasses.ccore.cutility.cutility import extractTextFromFilename, writeTextToFilename, readTextFromFilename

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

DEFAULT_SUMMARIZE_PROMPT = """
You are a high-fidelity summarizer. Your job is to preserve the original meaning, intent, and important details of the source content while making it concise and readable.

Summarize the content faithfully enough that a reader still understands:
- the main idea and purpose
- the most important facts, claims, arguments, and evidence
- key people, names, dates, numbers, and entities
- causal relationships, trade-offs, constraints, risks, and conclusions
- any actionable instructions or decisions that matter

Rules:
1. Do not invent, soften, or distort meaning.
2. Do not drop important details simply to save space.
3. Keep the original intent, tone, and emphasis.
4. Prefer accuracy over brevity when nuance matters.
5. Remove redundancy, not substance.
6. Preserve critical context, exceptions, and qualifiers.
7. Output only the final summary text, with no commentary or preamble.

Preferred style:
- concise but complete
- logically ordered
- easy to understand
- keeps the essential details even if the summary is longer than a generic summary
- if the content is complex, use a short structure like: main idea, supporting details, implications, and action items when relevant
"""

#------------------------------------------------------
# name: CLLMSettings
# desc: stores the LLM settings for prompt calls
#------------------------------------------------------
class CLLMSettings:
    def __init__(self, 
                 cllm=None,
                 max_tokens=8500, 
                 num_ctx=None,
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
        # Maximum number of tokens to generate in the reply. Higher values allow longer responses.
        self.num_ctx = num_ctx
        # Maximum context window in tokens for prompt + response. None lets Ollama use its default context size.
        self.temperature = temperature
        # Sampling randomness: lower values make output more deterministic; higher values make it more creative.
        self.top_p = top_p
        # Nucleus sampling cutoff: only the most likely token set whose cumulative probability is at most this value is considered.
        self.top_k = top_k
        # Limits sampling to the top K tokens at each step, which keeps generation more focused.
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
        # Optional stop sequence(s) that end generation early when encountered, such as the next-turn markers for a chat prompt.
        self.total_duration = 0
        # Total elapsed time for the request, including model load and generation overhead.
        self.load_duration = 0
        # Time spent loading the model before generation begins.
        self.prompt_eval_count = 0
        # Number of input tokens Ollama processed for the prompt.
        self.prompt_eval_duration = 0
        # Time spent processing the prompt tokens.
        self.eval_count = 0
        # Number of output tokens Ollama generated.
        self.eval_duration = 0
        # Time spent generating the output tokens.
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

    # setNumCtxByK() sets the context size in thousands of tokens (e.g., 4 = 4000 tokens)
    def setNumCtxByK(self, num_ctx_k):
        return self.setNumCtx(num_ctx_k * 1000)
    # end setNumCtxByK()

    def setNumCtx(self, num_ctx):
        self.num_ctx = num_ctx
        return self
    # end setNumCtx()

    def getNumCtx(self):
        return self.num_ctx
    # end getNumCtx()

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
    
    def getEvalCount(self):
        return self.eval_count
    # end getEvalCount()

    def getEvalDuration(self):
        return self.eval_duration
    # end getEvalDuration()

    def getPromptEvalCount(self):
        return self.prompt_eval_count
    # end getPromptEvalCount()

    def getPromptEvalDuration(self):
        return self.prompt_eval_duration
    # end getPromptEvalDuration()

    def getTotalDuration(self):
        return self.total_duration
    # end getTotalDuration()

    def getLoadDuration(self):
        return self.load_duration
    # end getLoadDuration()

    def getTokenStats(self):
        # Return a compact token budget summary based on the current conversation context.
        max_tokens = self.getNumCtx() if self.getNumCtx() is not None else self.max_tokens or 0
        used_tokens = self.getHistoryTokenSize() + self.getSystemPromptTokenSize()
        remaining_tokens = max(max_tokens - used_tokens, 0)
        return {
            "used": used_tokens,
            "remaining": remaining_tokens,
            "max": max_tokens,
        }
    # end getTokenStats()
    
    #---------------------------------------------------------------
    # prompting methods
    #---------------------------------------------------------------
    def _prompt(self, strprompt):
        #print(f"CLLM :: REQUEST ({self.model_platform}) model={self.model} num_ctx={self.num_ctx}")
        #print(strprompt)
        request_payload = {
            "prompt": strprompt,
            "model_platform": self.model_platform,
            "model": self.model,
            "num_ctx": self.num_ctx,
            "temperature": self.temperature,
            "top_p": self.top_p,
            "top_k": self.top_k,
            "max_tokens": self.max_tokens,
            "stream": self.stream,
            "stop": self.stop,
        }
        if self.model_platform == "Ollama":
            response = self._promptWithOllama(strprompt)
        else:
            llm = self._getLLM()
            response = llm.invoke(strprompt)
        # print(f"CLLM :: RESPONSE ({self.model_platform}) model={self.model} num_ctx={self.num_ctx}")
        # print(response)
        return response
    # end prompt()

    def _getOllamaHost(self):
        return self.api_base.rsplit("/v1", 1)[0] if self.api_base else "http://localhost:11434"
    # end _getOllamaHost()

    def _buildOllamaOptions(self):
        # Build the Ollama generation options from the class settings.
        options = {
            "temperature": self.temperature,
            "top_p": self.top_p,
            "top_k": self.top_k,
        }
        if self.num_ctx is not None:
            # Explicitly request a context window size for this request when set.
            options["num_ctx"] = self.num_ctx
        if self.stop:
            # Stop sequences prevent the model from continuing past the intended boundary.
            options["stop"] = self.stop
        return options
    # end _buildOllamaOptions()

    def _printDebugPayload(self, label, endpoint, payload):
        print(f"CLLM :: {label} {endpoint}")
        print(json.dumps(payload, indent=2, ensure_ascii=False, default=str))
    # end _printDebugPayload()

    def _logOllamaRequest(self, endpoint, payload):
        # Log and print the exact JSON payload sent to Ollama. Image paths are left as paths;
        # no image bytes/base64 are emitted by this logger.
        #self._printDebugPayload("Ollama REQUEST", endpoint, payload)
        #logger.warning(
        #    "CLLM :: Ollama REQUEST %s\n%s",
        #    endpoint,
        #    json.dumps(payload, indent=2, ensure_ascii=False, default=str),
        #)
        pass
    # end _logOllamaRequest()

    def _logOllamaResponse(self, endpoint, payload):
        # Log response metadata and token/timing statistics without duplicating the
        # potentially very large generated text body.
        response_summary = {
            key: value
            for key, value in payload.items()
            if key not in ("response", "message")
        }
        if isinstance(payload.get("message"), dict):
            message = payload["message"]
            response_summary["message"] = {
                "role": message.get("role"),
                "content_length": len(message.get("content", "") or ""),
            }
        if "response" in payload:
            response_summary["response_length"] = len(payload.get("response", "") or "")

        ##self._printDebugPayload("Ollama RESPONSE", endpoint, payload)
        ##logger.warning(
        ##    "CLLM :: Ollama RESPONSE %s\n%s",
        ##    endpoint,
        ##    json.dumps(response_summary, indent=2, ensure_ascii=False, default=str),
        ##)
    # end _logOllamaResponse()

    def _setOllamaResponseMetrics(self, payload):
        # Update token/timing counters from Ollama response payload.
        if not isinstance(payload, dict):
            return

        self.eval_count = int(payload.get("eval_count") or 0)
        self.eval_duration = int(payload.get("eval_duration") or 0)
        self.prompt_eval_count = int(payload.get("prompt_eval_count") or 0)
        self.prompt_eval_duration = int(payload.get("prompt_eval_duration") or 0)
        self.total_duration = int(payload.get("total_duration") or 0)
        self.load_duration = int(payload.get("load_duration") or 0)
    # end _setOllamaResponseMetrics()

    def _promptWithOllama(self, strprompt):
        options = self._buildOllamaOptions()
        endpoint = f"{self._getOllamaHost()}/api/generate"
        request_payload = {
            "model": self.model,
            "prompt": strprompt,
            "stream": False,
            "options": options,
        }

        self._logOllamaRequest(endpoint, request_payload)
        response = requests.post(
            endpoint,
            json=request_payload,
            timeout=300,
        )

        # Some local gateways expose only OpenAI-compatible /v1 routes. When
        # /api/generate is unavailable, retry against /v1/completions.
        if response.status_code == 404:
            completion_endpoint = f"{self.api_base.rstrip('/')}/completions"
            completion_payload = {
                "model": self.model,
                "prompt": strprompt,
                "temperature": self.temperature,
                "max_tokens": self.max_tokens,
                "top_p": self.top_p,
                "stream": False,
            }
            if self.stop:
                completion_payload["stop"] = self.stop

            self._logOllamaRequest(completion_endpoint, completion_payload)
            response = requests.post(
                completion_endpoint,
                json=completion_payload,
                timeout=300,
            )
            response.raise_for_status()
            payload = response.json()
            choices = payload.get("choices") or []
            if choices:
                return choices[0].get("text", "")
            return ""

        response.raise_for_status()
        payload = response.json()
        self._setOllamaResponseMetrics(payload)
        self._logOllamaResponse(endpoint, payload)
        return payload.get("response", "")
    # end _promptWithOllama()
        
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

    def _normalizeImagePaths(self, strpathimages):
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
        return normalized_paths
    # end _normalizeImagePaths()

    def _buildImageMessages(self, strprompt, normalized_paths):
        messages = []
        if getattr(self, "m_memoryon", False) and getattr(self, "m_systemprompt", ""):
            messages.append({"role": "system", "content": self.m_systemprompt})
        if getattr(self, "m_memoryon", False):
            for role, text in getattr(self, "m_history", []):
                role_name = role.lower()
                if role_name not in ("user", "assistant", "system"):
                    role_name = "user"
                messages.append({"role": role_name, "content": text})

        messages.append({
            "role": "user",
            "content": strprompt,
            "images": normalized_paths,
        })
        return messages
    # end _buildImageMessages()

    def _buildImageOptions(self, model_ctx):
        num_ctx = self.getNumCtx()
        if num_ctx is None:
            num_ctx = min(model_ctx, 4096)

        options = {
            "temperature": self.temperature,
            "top_p": self.top_p,
            "top_k": self.top_k,
            "num_ctx": num_ctx,
        }
        num_gpu = getattr(self, "m_imagenumgpu", None)
        if num_gpu is not None:
            options["num_gpu"] = num_gpu
        if self.stop:
            options["stop"] = self.stop
        return options
    # end _buildImageOptions()

    def _promptWithImages(self, strprompt, strpathimages):
        if self.model_platform != "Ollama":
            raise ValueError("Image prompts are only supported on the Ollama platform.")

        normalized_paths = self._normalizeImagePaths(strpathimages)
        host = self._getOllamaHost()
        chat_timeout = getattr(self, "m_imagechattimeout", 300)
        client = ollama.Client(host=host, timeout=chat_timeout)

        messages = self._buildImageMessages(strprompt, normalized_paths)
        model_ctx = self.getModelContextSize()
        options = self._buildImageOptions(model_ctx)
        logger.warning(
            "CLLM :: _promptWithImages() using model=%s num_ctx=%s options=%s",
            self.model,
            options.get("num_ctx"),
            options,
        )

        request_payload = {
            "model": self.model,
            "messages": messages,
            "options": options,
        }
        endpoint = f"{host}/api/chat"
        self._logOllamaRequest(endpoint, request_payload)

        try:
            response = client.chat(
                model=self.model,
                messages=messages,
                options=options,
            )
        except Exception as e:
            logger.error(
                f"Failure: CLLM :: _promptWithImages() - client.chat() failed "
                f"(timeout={chat_timeout}s): {type(e).__name__}: {e}"
            )
            raise

        response_payload = dict(response) if not isinstance(response, dict) else response
        self._setOllamaResponseMetrics(response_payload)
        self._logOllamaResponse(endpoint, response_payload)
        return response.get("message", {}).get("content", "")
    # end _promptWithImages()

    #--------------------------------------------------------------------------
    # helper functions for initializing the llm chat/non chat platforms
    #--------------------------------------------------------------------------
    def _getLLM(self, params=None):
        builders = {
            "ChatOpenAI": self._get_chat_openai_platform,
            "OpenAI": self._get_openai_platform,
            "Ollama": self._get_ollama_platform,
        }
        builder = builders.get(self.model_platform)
        if builder is None:
            logger.warning(f"Failure: CLLMSettings :: _getLLM() - Unsupported platform: {self.model_platform}")
            return None
        return builder(params=params)
    # end getLLM()  

    def _merge_params(self, base_params, params=None):
        merged = dict(base_params)
        if params:
            merged.update(params)
        return merged
    # end _merge_params()

    def _get_common_platform_params(self):
        return {
            "model": self.model,
            "temperature": self.temperature,
        }
    # end _get_common_platform_params()
    
    def _get_chat_openai_platform(self, params=None):
        from langchain_community.chat_models.openai import ChatOpenAI

        base_params = self._get_common_platform_params()
        base_params["max_tokens"] = self.max_tokens
        return ChatOpenAI(**self._merge_params(base_params, params))
    # end _init_chat_openai_platform()
    
    def _get_openai_platform(self, params=None):
        from langchain_community.llms.openai import OpenAI

        base_params = self._get_common_platform_params()
        # Specify the non-chat model
        base_params["max_tokens"] = self.max_tokens
        return OpenAI(**self._merge_params(base_params, params))
    # end _init_openai_platform()
    
    def _get_ollama_platform(self, params=None):
        from langchain_community.llms.ollama import Ollama

        base_params = self._get_common_platform_params()
        base_params["top_p"] = self.top_p
        base_params["stop"] = self.stop
        if self.num_ctx is not None:
            base_params["num_ctx"] = self.num_ctx
        return Ollama(**self._merge_params(base_params, params))
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
        super().__init__(cllm=self) 
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

    def getHistoryTokenSize(self):
        # total token size of the conversation history context
        size = 0
        for role, text in self.m_history:
            size += self.countTokens(f"{role}: {text}")
        return size
    # end getHistoryTokenSize()

    def getSystemPromptTokenSize(self):
        # total token size of the system prompt context
        if self.m_systemprompt:
            return self.countTokens(f"System: {self.m_systemprompt}")
        return 0
    # end getSystemPromptTokenSize()

    def getContextTokenSize(self):
        # maximum token size available for the current prompt/response context
        if self.getNumCtx() is not None:
            return self.getNumCtx()
        if self.max_tokens is not None:
            return self.max_tokens
        return 0
    # end getContextTokenSize()

    def getRemainingTokens(self):
        # remaining token size available for the next prompt/response
        context_size = self.getContextTokenSize()
        used_size = self.getHistoryTokenSize() + self.getSystemPromptTokenSize()
        remaining_size = context_size - used_size
        return max(remaining_size, 0)
    # end getRemainingTokens()
    
    def getRemainingContextTokenSize(self):
        # remaining token size available for the next prompt
        return self.getRemainingTokens()
    # end getRemainingContextSize()

    def _estimate_tokens(self, text):
        if not text:
            return 0
        return (len(str(text).encode("utf-8")) + 3) // 4
    # end _estimate_tokens()

    def countTokens(self, text):
        # Estimate tokens from UTF-8 byte length using 1 token ~= 4 bytes.
        return self._estimate_tokens(text)
    # end countTokens()

    def getModelContextSize(self, default=8192):
        # query the running Ollama server for the model's real context length (in tokens) ------------------------------------------------
        if self.model_platform != "Ollama":
            logger.warning("Failure: CLLM :: getModelContextSize() - only supported for the Ollama platform.")
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
            logger.warning("Failure: CLLM :: getModelContextSize() - context_length not found in model info.")
        except Exception as e:
            logger.error(f"Failure: CLLM :: getModelContextSize() - Error querying Ollama: {e}")
        return default
    # end getModelContextSize()

    def _buildConversation(self, strprompt):
        # assemble the fu-------------------ll conversation transcript for a memory-enabled prompt
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
        response = self._prompt(strprompt)
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
            strtext = self._prompt(strprompt)
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
        self.m_strprompt = strprompt
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

    def promptImage(self, strprompt, strpathimages): 
        self.m_strprompt = strprompt
        strresponse = self._promptWithImages(strprompt, strpathimages)
        strresponse = strresponse.strip() if strresponse else strresponse
        if self.m_memoryon:
            self.m_history.append(("User", strprompt))
            self.m_history.append(("Assistant", strresponse))
        self.m_strpromptresponse = strresponse
        return self.m_strpromptresponse
    # end promptImage()
    
    def chain(self, strprompt, docs):
        self.m_strpromptresponse = self._chain(strprompt, docs)
        return self.m_strpromptresponse
    # end chain()

    def compressPrompt(self, strprompt, itokensizetocompress): 
        strprompt_length = len(str(strprompt)) if strprompt is not None else 0

        def _fit_segments(segments):
            fitted_prompt = ""
            for segment in segments:
                segment = segment.strip()
                if not segment:
                    continue
                candidate_text = f"{fitted_prompt} {segment}".strip() if fitted_prompt else segment
                if self._estimate_tokens(candidate_text) > target_tokens:
                    break
                fitted_prompt = candidate_text
            return fitted_prompt

        def _strip_dangling_tail(text):
            dangling_words = {
                "a", "an", "and", "as", "at", "before", "by", "for", "from",
                "in", "into", "must", "of", "on", "or", "the", "to", "with",
            }
            words = text.split()
            while words:
                last_word = re.sub(r"[^A-Za-z]", "", words[-1]).lower()
                if last_word and last_word in dangling_words:
                    words.pop()
                    continue
                break
            return " ".join(words).rstrip(" ,;:-")

        def _ensure_complete_sentence(text):
            text = _strip_dangling_tail((text or "").strip())
            if not text:
                return ""

            completion_markers = (".", "!", "?")
            if text.endswith(completion_markers):
                return text

            # Prefer trimming back to the last clause boundary instead of leaving a fragment.
            trimmed_text = re.sub(r"[,:;\-]+$", "", text).strip()
            clause_boundary = max(trimmed_text.rfind(","), trimmed_text.rfind(";"), trimmed_text.rfind(":"))
            if clause_boundary != -1:
                candidate_text = trimmed_text[:clause_boundary].strip()
                candidate_text = _strip_dangling_tail(candidate_text)
                if candidate_text:
                    trimmed_text = candidate_text

            if not trimmed_text:
                trimmed_text = text.rstrip(" ,;:-")

            with_period = f"{trimmed_text}."
            if self._estimate_tokens(with_period) <= target_tokens:
                return with_period
            if self._estimate_tokens(trimmed_text) <= target_tokens:
                return trimmed_text

            shortened_text = trimmed_text
            while shortened_text:
                shortened_text = _strip_dangling_tail(shortened_text.rsplit(" ", 1)[0] if " " in shortened_text else "")
                if not shortened_text:
                    break
                with_period = f"{shortened_text}."
                if self._estimate_tokens(with_period) <= target_tokens:
                    return with_period
                if self._estimate_tokens(shortened_text) <= target_tokens:
                    return shortened_text
            return ""

        if not strprompt:
            self.m_strpromptresponse = ""
            return ""

        try:
            target_tokens = int(itokensizetocompress)
        except (TypeError, ValueError):
            logger.warning("Failure: CLLM :: compressPrompt() - Invalid token target. Returning original prompt.")
            self.m_strpromptresponse = strprompt
            return strprompt

        if target_tokens <= 0:
            logger.warning("Failure: CLLM :: compressPrompt() - Token target must be > 0. Returning original prompt.")
            self.m_strpromptresponse = strprompt
            return strprompt

        original_prompt = str(strprompt)
        current_prompt = original_prompt
        current_tokens = self._estimate_tokens(current_prompt)
        if current_tokens <= target_tokens:
            self.m_strpromptresponse = current_prompt
            return current_prompt

        # Iteratively compress toward the requested token budget.
        for pass_index in range(3):
            current_tokens = self._estimate_tokens(current_prompt)
            if current_tokens <= target_tokens:
                break

            compression_prompt = (
                "Compress the prompt below to fit within the specified token budget.\n"
                "Preserve all essential intent, requirements, constraints, conditions, priorities, and output-format instructions.\n"
                "Remove redundancy, verbosity, examples, and nonessential wording where possible without changing meaning.\n"
                "Do not add new requirements or weaken existing ones.\n"
                f"Maximum length: {target_tokens} tokens.\n"
                "Output only the compressed prompt, with no commentary, explanation, or formatting wrapper.\n\n"
                f"Prompt to compress:\n{current_prompt}"
            )

            candidate_prompt = self._prompt(compression_prompt)
            if not candidate_prompt:
                logger.warning("Failure: CLLM :: compressPrompt() - No response from model during compression.")
                break

            candidate_prompt = candidate_prompt.strip()
            if not candidate_prompt:
                logger.warning("Failure: CLLM :: compressPrompt() - Empty compression result.")
                break

            candidate_tokens = self._estimate_tokens(candidate_prompt)
            if candidate_tokens >= current_tokens:
                logger.warning("Warning: CLLM :: compressPrompt() - Compression did not reduce tokens.")
                break

            current_prompt = candidate_prompt

        # Fallback: retry with a stricter instruction, then trim only at sentence boundaries.
        if self._estimate_tokens(current_prompt) > target_tokens:
            strict_compression_prompt = (
                "Rewrite the prompt to preserve the same intent, constraints, and key details.\n"
                f"Hard limit: <= {target_tokens} tokens.\n"
                "Return only complete sentences, and do not end with a partial phrase.\n"
                "Return only the rewritten prompt text.\n\n"
                f"Prompt:\n{current_prompt}"
            )
            strict_candidate = self._prompt(strict_compression_prompt)
            if strict_candidate:
                strict_candidate = strict_candidate.strip()
                if strict_candidate and self._estimate_tokens(strict_candidate) <= target_tokens:
                    current_prompt = strict_candidate

        if self._estimate_tokens(current_prompt) > target_tokens:
            sentence_parts = re.split(r"(?<=[.!?])\s+", current_prompt.strip())
            best_prompt = _fit_segments(sentence_parts)
            if best_prompt:
                current_prompt = best_prompt

        if self._estimate_tokens(current_prompt) > target_tokens:
            clause_parts = re.split(r"(?<=[,;:.])\s+", current_prompt.strip())
            best_prompt = _fit_segments(clause_parts)
            if best_prompt:
                current_prompt = best_prompt

        if self._estimate_tokens(current_prompt) > target_tokens:
            words = current_prompt.split()
            best_prompt = _fit_segments(words)
            best_prompt = _strip_dangling_tail(best_prompt)
            if best_prompt:
                current_prompt = best_prompt

        current_prompt = _ensure_complete_sentence(current_prompt) or current_prompt

        final_tokens = self._estimate_tokens(current_prompt)
        if final_tokens > target_tokens:
            # Final hard cap by bytes to strictly satisfy the requested token budget.
            max_bytes = target_tokens * 4
            strict_prompt = current_prompt.encode("utf-8")[:max_bytes].decode("utf-8", errors="ignore")
            strict_prompt = _ensure_complete_sentence(strict_prompt) or _strip_dangling_tail(strict_prompt)
            current_prompt = strict_prompt if strict_prompt else current_prompt[:max_bytes]
            final_tokens = self._estimate_tokens(current_prompt)

        if final_tokens > target_tokens:
            logger.warning(
                "Warning: CLLM :: compressPrompt() - Strict limit fallback still exceeded target (%s > %s).",
                final_tokens,
                target_tokens,
            )

        print(f"  compressPrompt: {strprompt_length} chars -> {final_tokens}/{target_tokens} tokens ({len(current_prompt)} chars)")

        self.m_strpromptresponse = current_prompt
        return current_prompt
    # end compressPrompt()

    def compressContent(self, strcontenttosummarize, strcurrentsummary="", strhowtosummarizeprompt="", isummarymaxsize=8000):
        if not strhowtosummarizeprompt or not strhowtosummarizeprompt.strip():
            strhowtosummarizeprompt = DEFAULT_SUMMARIZE_PROMPT

        # get the lengths of the prompts and content
        icontenttosummarize = len(strcontenttosummarize)
        icurrentsummary = len(strcurrentsummary)
        ihowtosummarizeprompt = len(strhowtosummarizeprompt)
        print(f"compressContent: content={icontenttosummarize} chars, max={isummarymaxsize}")
    
        # compress the current summary and how-to-summarize prompt if they exceed the maximum summary size
        if(icurrentsummary >= isummarymaxsize * 0.3):
            strcurrentsummary = self.compressPrompt(strcurrentsummary, isummarymaxsize * 0.3)
            icurrentsummary = len(strcurrentsummary)
        # end if 
        
        # compress the current summary and how-to-summarize prompt if they exceed the maximum summary size
        if(ihowtosummarizeprompt >= isummarymaxsize * 0.2):
            strhowtosummarizeprompt = self.compressPrompt(strhowtosummarizeprompt, isummarymaxsize * 0.2)
            ihowtosummarizeprompt = len(strhowtosummarizeprompt)
        # end if

        # compute the content size that can be summarized based on the maximum summary size
        # compute how much space is available for the content to summarize in the prompt 
        icontenttosummarizepagesize = isummarymaxsize - (ihowtosummarizeprompt + icurrentsummary)       
        # compute the number of pages of content to summarize based on the available space
        inumofpagesofcontenttosummarize = math.ceil(icontenttosummarize / icontenttosummarizepagesize)
        print(f"  pages={inumofpagesofcontenttosummarize}, page_size={icontenttosummarizepagesize} chars")

        for i in range(inumofpagesofcontenttosummarize):
            # compute the start and end indices for the current page of content to summarize
            istartindex = i * icontenttosummarizepagesize
            iendindex = min((i + 1) * icontenttosummarizepagesize, icontenttosummarize)
            strcontenttoprocess = strcontenttosummarize[istartindex:iendindex]
            # build the prompt for summarization it includes the how-to-summarize prompt, the current summary, and the content to summarize
            strprompt = f"{strhowtosummarizeprompt}\n\nCurrent Summary:\n{strcurrentsummary}\n\nContent to Summarize:\n{strcontenttoprocess}"
        
            # generate the summary for the current page of content - compress the prompt if it exceeds the maximum summary size
            strsummaryresponse = self.compressPrompt(self._prompt(strprompt),isummarymaxsize * 0.3)
        
            # update the current summary with the new summary response
            if strsummaryresponse:
                strcurrentsummary = f"{strsummaryresponse.strip()}"
            print(f"  [{i + 1}/{inumofpagesofcontenttosummarize}] chunk={len(strcontenttoprocess)} chars, prompt={len(strprompt)} chars, summary={len(strcurrentsummary)} chars")
        # end for
        return strcurrentsummary  
    # end compressContent()

    def promptContentToFile(self, strsystemprompt, strnewcontent, strfilename, icontextsize=8000):
        # Generate a prompt response based on the provided content and save it to a file.
        previous_memory_state = self.m_memoryon
        previous_system_prompt = self.m_systemprompt
        self.disableMemory()  # Disable memory to avoid including previous conversation context.

        # Read existing file content when present; otherwise start from empty content.
        if os.path.exists(strfilename):
            with open(strfilename, "r", encoding="utf-8") as f:
                strfilenamecontent = f.read()
        else:
            strfilenamecontent = ""

        request_prompt = (
            f"{strsystemprompt}\n\n"
            f"New Content:\n{strnewcontent}\n\n"
            f"Existing Content:\n{strfilenamecontent}"
        )

        # if the request prompt exceeds the context size, truncate the new content to fit within the limit
        if self.countTokens(request_prompt) > icontextsize:
            logger.warning("Warning: CLLM :: promptContentToFile() - Request prompt exceeds context size. Truncating new content.")
            # Calculate the maximum allowed length for new content
            max_new_content_length = icontextsize - self.countTokens(f"{strsystemprompt}\n\nExisting Content:\n{strfilenamecontent}")
            if max_new_content_length > 0:
                truncated_new_content = strnewcontent[:max_new_content_length]
                request_prompt = (
                    f"{strsystemprompt}\n\n"
                    f"New Content:\n{truncated_new_content}\n\n"
                    f"Existing Content:\n{strfilenamecontent}"
                )
            else:
                logger.error("Error: CLLM :: promptContentToFile() - Not enough context size to include any new content.")
                return
            
            

        self.m_strpromptresponse = self._prompt(request_prompt)

        if self.m_strpromptresponse:
            writeTextToFilename(strfilename, self.m_strpromptresponse)
            logger.info(f"Success: CLLM :: promptContentToFile() - Response saved to {strfilename}.")
        else:
            logger.warning("Failure: CLLM :: promptContentToFile() - No response returned from _prompt.")

        if previous_memory_state:
            self.enableMemory(previous_system_prompt)
        else:
            self.disableMemory()
    # end promptContentToFile()
       
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