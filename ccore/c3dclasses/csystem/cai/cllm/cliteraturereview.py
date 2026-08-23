import json
import logging
import os
import time

from c3dclasses.csystem.cai.cllm.cliteraturereviewprompts import (
    LITERATURE_REVIEW_SYSTEM_PROMPT,
    DEFAULT_LITERATURE_REVIEW_PROMPT,
)

from c3dclasses.ccore.cutility.cutility import text2id, readJSONFromFilename, extractTextFromFilename

class CLiteratureReview:
    def __init__(self, cllm=None):
        self.m_cllm = cllm
        self.m_researchquestions = []
        self.m_literaturefilenames = []
        self.m_review = {
            "articles": {},
            "researchquestions": {},
            "reviews": {}
        }
        self.m_reviewfilename = ""
        self.m_defaultprompt = DEFAULT_LITERATURE_REVIEW_PROMPT
        logging.getLogger("CUtility").setLevel(logging.WARNING)
        print("CLiteratureReview initialized.")
    # end __init__())

    def create(self, cllm=None, strreviewfilename=""):
        if cllm is not None:
            self.m_cllm = cllm
        self.m_reviewfilename = strreviewfilename

        if strreviewfilename:
            review_dir = os.path.dirname(strreviewfilename)
            if review_dir:
                os.makedirs(review_dir, exist_ok=True)
            if os.path.exists(strreviewfilename):
                self.m_review = readJSONFromFilename(strreviewfilename)
            else:
                self.m_review = {
                    "articles": {},
                    "researchquestions": {},
                    "reviews": {}
                }
        else:
            self.m_review = {
                "articles": {},
                "researchquestions": {},
                "reviews": {}
            }

        print(f"CLiteratureReview created with review filename: {strreviewfilename}")
    # end create()

    def useDefaultPrompts(self, bUseDefaultPrompts=True):
        if bUseDefaultPrompts:
            self.m_defaultprompt = DEFAULT_LITERATURE_REVIEW_PROMPT
        else:
            self.m_defaultprompt = LITERATURE_REVIEW_SYSTEM_PROMPT

    def addResearchQuestion(self, strresearchquestion):
        self.m_researchquestions.append(strresearchquestion)
        if self.m_cllm is not None:
            print(f"Added research question token size: {self.m_cllm.countTokens(strresearchquestion)}")
    # end addResearchQuestion()

    def addLiteratureFilename(self, strliteraturefilename):
        self.m_literaturefilenames.append(strliteraturefilename)
        if self.m_cllm is not None:
            print(f"Added literature filename token size: {self.m_cllm.countTokens(strliteraturefilename)}")
    # end addLiteratureFilename()

    def reviewLiterature(self):
        print("\n=== Literature review started ===")
        for strliteraturefilename in self.m_literaturefilenames:
            print(f"\nArticle: {os.path.basename(strliteraturefilename)}")
            strliterature = extractTextFromFilename(strliteraturefilename)
            text2id_literaturefilename = text2id(strliteraturefilename)

            self.m_review["articles"].setdefault(text2id_literaturefilename, {
                "filename": strliteraturefilename,
                "text": strliterature,
            })

            for strresearchquestion in self.m_researchquestions:
                text2id_researchquestion = text2id(strresearchquestion)

                self.m_review["researchquestions"].setdefault(text2id_researchquestion, {
                    "question": strresearchquestion,
                })

                if text2id_researchquestion in self.m_review["reviews"].get(text2id_literaturefilename, {}):
                    print(f"  - Skipping duplicate: '{strresearchquestion}'")
                    continue

                start_time = time.perf_counter()
                print(f"  - Evaluating RQ: '{strresearchquestion}'")
                response = self.evaluateResearchQuestion(strresearchquestion, strliterature)
                elapsed = time.perf_counter() - start_time
                print(f"  - Evaluation Completed in {elapsed:.2f} seconds")

                self.m_review["reviews"].setdefault(text2id_literaturefilename, {})
                self.m_review["reviews"][text2id_literaturefilename][text2id_researchquestion] = response
                self.saveReview()
            # end for
        # end for
        print("=== Literature review complete ===\n")
    # end reviewLiterature()

    def saveReview(self):
        if self.m_reviewfilename:
            review_dir = os.path.dirname(self.m_reviewfilename)
            if review_dir:
                os.makedirs(review_dir, exist_ok=True)
            with open(self.m_reviewfilename, 'w', encoding='utf-8') as file:
                json.dump(self.m_review, file, ensure_ascii=False, indent=4)
            # end with
            print(f"Saved review to: {self.m_reviewfilename}")
        # end if
        else:
            print("No review filename specified. Review not saved.")
        # end else
    # end saveReview()

    def evaluateResearchQuestion(self, strresearchquestion, strliterature):
        prompt_template = self.m_defaultprompt or DEFAULT_LITERATURE_REVIEW_PROMPT
        strprompt = prompt_template.format(
            research_questions=strresearchquestion,
            literature=strliterature,
        )

        prompt_tokens = self.m_cllm.countTokens(strprompt)
        literature_tokens = self.m_cllm.countTokens(strliterature)
        total_input_tokens = prompt_tokens + literature_tokens

        response = self.m_cllm.prompt(strprompt)
        response_tokens = self.m_cllm.countTokens(response) if response else 0

        print(f"    Prompt tokens: {prompt_tokens}")
        print(f"    Literature tokens: {literature_tokens}")
        print(f"    Total input tokens: {total_input_tokens}")
        print(f"    Response tokens: {response_tokens}")
        return response
    # end evaluateResearchQuestion()
# end class CLLM

