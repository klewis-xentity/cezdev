#---------------------------------------------------
# file: cllmsettings.py
# desc: stores the llm configuration and platform settings
#---------------------------------------------------
import json
import logging
from pathlib import Path

import ollama
import requests

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
