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

from .cllmsettings import CLLMSettings
from .cllmcontext import CLLMContext
from .cllmprompts import (
    DEFAULT_SUMMARIZE_PROMPT,
    PROMPT_WITH_FORMAT_TEMPLATE,
    PROMPT_MODIFY_TEMPLATE,
    PROMPT_COMPRESS_TEMPLATE,
    PROMPT_RESPONSE_OBJECT_TEMPLATE,
    PROMPT_RESPONSE_VALIDATION_TEMPLATE,
    PROMPT_CONFIDENCE_SCORE_TEMPLATE,
    PROMPT_CONFIDENCE_REASON_TEMPLATE,
)
#---------------------------------------------------------------------
# name: CLLM
# desc: define an object that operates on a large language model
#---------------------------------------------------------------------
class CLLM2 (CLLMSettings): 
    def __init__(self):
        self.m_strprompt = ""
        self.m_strpromptresponse = ""
        super().__init__(cllm=self) 
    # end __init__()    
               
    #------------------------------------------
    # member access methods
    #------------------------------------------
    def getPrompt(self): 
        return self.m_strprompt
    # end getPrompt()

    # prompt response
    def getPromptResponse(self):
        return self.m_strpromptresponse
    # end getPromptResponse()

    #------------------------------------------
    # prompting methods
    #------------------------------------------
    def prompt(self, strprompt): 
        self.m_strprompt = strprompt
        self.m_strpromptresponse = self._prompt(strprompt)
        return self.m_strpromptresponse
    # end prompt()  

    def promptWithFormat(self, strprompt, format=None):
        self.m_strprompt = strprompt
        if format:
            self.m_strpromptresponse = self._prompt(
                PROMPT_WITH_FORMAT_TEMPLATE.format(output_format=format, prompt=strprompt)
            )
        else:
            self.m_strpromptresponse = self._prompt(strprompt)
        return self.m_strpromptresponse
    # end promptWithFormat()

    def promptWithTemplate(self, strtemplate, **params):
        pass
    # end promptWithTemplate()

    def promptAgain(self): 
        if self.m_strprompt:
            self.m_strpromptresponse = self._prompt(self.m_strprompt)
            return self.m_strpromptresponse
        # end if
        return None
    # end promptAgain()

    def promptModify(self, strprompt, inumtokens=None, strconstraints=""):
        # Backward compatibility: allow promptModify(prompt, "constraints") calls.
        if isinstance(inumtokens, str) and not strconstraints:
            strconstraints = inumtokens
            inumtokens = None
        # end if

        if inumtokens is None:
            modification_prompt = PROMPT_MODIFY_TEMPLATE.format(
                prompt=strprompt,
                constraints=strconstraints,
            )
            modified_prompt = self._prompt(modification_prompt)
            return modified_prompt.strip()
        # end if

        compression_prompt = PROMPT_COMPRESS_TEMPLATE.format(
            tokens=inumtokens,
            prompt=strprompt,
            constraints=strconstraints,
        )
        compressed_prompt = self._prompt(compression_prompt)
        return compressed_prompt.strip()
    # end promptModify()
 
    def promptCLLMContext(self, strprompt, cllmconversationalcontext=None, cllmcontexts=None):
        self.m_strprompt = strprompt
        context_parts = []
        strglobalcontext = CLLMContext.getGlobalContext()
        if strglobalcontext:
            context_parts.append(strglobalcontext)
        # end if
        if cllmcontexts:
            for cllmcontext in cllmcontexts:
                strcontext = cllmcontext.buildContext()
                if strcontext:
                    context_parts.append(strcontext)
                # end if
            # end for
        # end if

        strcllmcontexts = "\n".join(context_parts)
        if strcllmcontexts:
            strcllmcontexts += "\n"
        # end if

        if(cllmconversationalcontext):
            strresponse = self.prompt(strcllmcontexts + cllmconversationalcontext.buildContext(strprompt))
            cllmconversationalcontext.addAssistantContext(strresponse)       
            return strresponse
        # end if
        return self.prompt(strcllmcontexts + strprompt)
    # end prompt()

    #-------------------------------------
    # response parsing methods
    #-------------------------------------
    def response(self):
        return self.m_strpromptresponse
    # end response()
    
    def responseToBoolean(self):
        if self.m_strpromptresponse:
            response_lower = self.m_strpromptresponse.strip().lower()
            if response_lower in ["true", "yes", "1"]:
                return True
            # end if
            elif response_lower in ["false", "no", "0"]:
                return False
            # end if
        # end if
        return None
    # end responseToBoolean()

    def responseToInteger(self):
        if self.m_strpromptresponse:
            try:
                return int(self.m_strpromptresponse.strip())
            except ValueError:
                return None
        # end if
        return None
    # end responseToInteger()

    def responseToFloat(self):
        if self.m_strpromptresponse:
            try:
                return float(self.m_strpromptresponse.strip())
            except ValueError:
                return None
        # end if
        return None
    # end responseToFloat()

    def responseToJSON(self):
        return self.parseJSON()
    # end responseToJSON()

    def responseToCode(self, strtype):
        return self.parseCode(strtype)
    # end responseToCode()

    def responseToAllCode(self):
        return self.parseAllCode()
    # end responseToAllCode()

    def responseToObject(self, strformat=None):
        if self.m_strpromptresponse:
            format_prompt = PROMPT_RESPONSE_OBJECT_TEMPLATE.format(
                output_format=strformat,
                response=self.m_strpromptresponse,
            )
            formatted_response = self._prompt(format_prompt)
            return formatted_response.strip()
        # end if
        return None
    # end responseToObject()

    def isResponseValid(self):
        if self.m_strpromptresponse:
            # ask the llm to validate it's own response
            validation_prompt = PROMPT_RESPONSE_VALIDATION_TEMPLATE.format(
                prompt=self.m_strprompt,
                response=self.m_strpromptresponse,
            )
            validation_result = self._prompt(validation_prompt)
            return validation_result.strip().lower() == "yes"
        # end if
        return False
    # end isResponseValid()

    def responseConfidenceScore(self):
        if self.m_strpromptresponse:
            # Ask the LLM for a machine-readable score only.
            confidence_prompt = PROMPT_CONFIDENCE_SCORE_TEMPLATE.format(
                prompt=self.m_strprompt,
                response=self.m_strpromptresponse,
            )
            confidence_result = self._prompt(confidence_prompt)
            if confidence_result is None:
                return None

            score_text = str(confidence_result).strip()
            try:
                score = float(score_text)
            except ValueError:
                # Fallback: extract the first numeric token from verbose responses
                # such as "Confidence: 0.82" or "82%".
                match = re.search(r"-?\d+(?:\.\d+)?", score_text)
                if not match:
                    return None
                try:
                    score = float(match.group(0))
                except ValueError:
                    return None

            if "%" in score_text or (score > 1.0 and score <= 100.0):
                score = score / 100.0

            # Keep score in [0, 1] to guarantee a consistent API contract.
            if score < 0.0:
                score = 0.0
            elif score > 1.0:
                score = 1.0
            return score
        # end if
        return None
    # end responseConfidenceScore()

    def responseConfidenceReason(self):
        if self.m_strpromptresponse:
            reason_prompt = PROMPT_CONFIDENCE_REASON_TEMPLATE.format(
                prompt=self.m_strprompt,
                response=self.m_strpromptresponse,
            )
            reason_result = self._prompt(reason_prompt)
            if reason_result is None:
                return None
            reason = str(reason_result).strip()
            return reason if reason else None
        # end if
        return None
    # end responseConfidenceReason()

    def isResponseDelusional(self):
        return not self.isResponseValid()
    # end isResponseDelusional()
# end CLLM2