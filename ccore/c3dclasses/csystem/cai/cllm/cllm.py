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

from .cllmsettings import CLLMSettings

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

        placeholder_pattern = re.compile(r"\{[^{}]+\}")

        def _extract_placeholders(text):
            return placeholder_pattern.findall(text or "")

        def _protect_placeholders(text):
            placeholders = _extract_placeholders(text)
            protected_text = text or ""
            token_map = []
            for index, placeholder in enumerate(placeholders):
                token = f"__CLLM_PLACEHOLDER_{index}__"
                protected_text = protected_text.replace(placeholder, token)
                token_map.append((token, placeholder))
            return protected_text, token_map

        def _restore_placeholders(text, token_map):
            restored_text = text or ""
            for token, placeholder in token_map:
                restored_text = restored_text.replace(token, placeholder)
            return restored_text

        def _preserves_placeholders(candidate_text, token_map):
            restored_text = _restore_placeholders(candidate_text, token_map)
            return all(placeholder in restored_text for _, placeholder in token_map), restored_text

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
        protected_original_prompt, placeholder_token_map = _protect_placeholders(original_prompt)
        current_prompt = protected_original_prompt
        current_tokens = self._estimate_tokens(current_prompt)
        if current_tokens <= target_tokens:
            _, restored_prompt = _preserves_placeholders(current_prompt, placeholder_token_map)
            self.m_strpromptresponse = restored_prompt
            return restored_prompt

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

            candidate_ok, restored_candidate_prompt = _preserves_placeholders(candidate_prompt, placeholder_token_map)
            if not candidate_ok:
                logger.warning("Warning: CLLM :: compressPrompt() - Compression removed a {variable} placeholder; keeping the previous prompt.")
                break

            candidate_prompt = restored_candidate_prompt

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
                if strict_candidate:
                    strict_candidate_ok, restored_strict_candidate = _preserves_placeholders(strict_candidate, placeholder_token_map)
                    if strict_candidate_ok and self._estimate_tokens(restored_strict_candidate) <= target_tokens:
                        current_prompt = restored_strict_candidate

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
        current_prompt = _restore_placeholders(current_prompt, placeholder_token_map)

        if not all(placeholder in current_prompt for _, placeholder in placeholder_token_map):
            logger.warning("Warning: CLLM :: compressPrompt() - Placeholder preservation failed; returning the original prompt unchanged.")
            current_prompt = original_prompt

        final_tokens = self._estimate_tokens(current_prompt)
        if final_tokens > target_tokens:
            # Final hard cap by bytes to strictly satisfy the requested token budget.
            max_bytes = target_tokens * 4
            strict_prompt = current_prompt.encode("utf-8")[:max_bytes].decode("utf-8", errors="ignore")
            strict_prompt = _ensure_complete_sentence(strict_prompt) or _strip_dangling_tail(strict_prompt)
            strict_prompt = _restore_placeholders(strict_prompt, placeholder_token_map)
            if all(placeholder in strict_prompt for _, placeholder in placeholder_token_map):
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
            #strprompt = f"{strhowtosummarizeprompt}\n\nCurrent Summary:\n{strcurrentsummary}\n\nContent to Summarize:\n{strcontenttoprocess}"
            #strprompt = f"{strhowtosummarizeprompt}\n\nCurrent Summary:\n{strcurrentsummary}\n\nContent to Summarize:\n{strcontenttoprocess}"
            #strprompt = f"{strhowtosummarizeprompt}\n\nCurrent Summary:\n{strcurrentsummary}\n\nContent to Summarize:\n{strcontenttoprocess}"

            strprompt = strhowtosummarizeprompt.format(
                        strcontenttosummarize=strcontenttoprocess,
            )


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