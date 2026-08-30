import json
import logging
import os
import time

from c3dclasses.csystem.cai.cllm.cliteraturereviewprompts import (
    LITERATURE_REVIEW_SYSTEM_PROMPT,
    DEFAULT_LITERATURE_REVIEW_PROMPT,
)

from c3dclasses.ccore.cutility.cutility import text2id, readJSONFromFilename, extractTextFromFilename


def contextBlocks(strliterature, icontextbudget):
    """Yield context-sized literature chunks using a conservative token estimate.

    We approximate 1 token ~= 4 UTF-8 bytes and reserve part of the budget for
    instructions, question text, and model output.
    """
    text = str(strliterature or "")
    if not text.strip():
        return [""]

    try:
        context_budget = int(icontextbudget) if icontextbudget is not None else 8000
    except (TypeError, ValueError):
        context_budget = 8000

    if context_budget <= 0:
        context_budget = 8000

    # Keep room for prompt scaffolding + response.
    available_tokens = max(int(context_budget * 0.45), 500)
    max_bytes_per_chunk = max(available_tokens * 4, 2000)

    # Prefer sentence-level chunking for better coherence.
    sentence_parts = [s.strip() for s in text.replace("\r\n", "\n").split("\n") if s.strip()]
    chunks = []
    current_chunk = ""

    for part in sentence_parts:
        candidate = f"{current_chunk}\n\n{part}".strip() if current_chunk else part
        candidate_bytes = len(candidate.encode("utf-8"))
        if current_chunk and candidate_bytes > max_bytes_per_chunk:
            chunks.append(current_chunk)
            current_chunk = part
            # Extremely long single paragraph fallback: split by words.
            while len(current_chunk.encode("utf-8")) > max_bytes_per_chunk:
                words = current_chunk.split()
                if len(words) <= 1:
                    head = current_chunk.encode("utf-8")[:max_bytes_per_chunk].decode("utf-8", errors="ignore")
                    head = head.strip()
                    if head:
                        chunks.append(head)
                    current_chunk = current_chunk[len(head):].strip()
                    if not current_chunk:
                        break
                    continue

                temp = ""
                index = 0
                while index < len(words):
                    next_temp = f"{temp} {words[index]}".strip() if temp else words[index]
                    if len(next_temp.encode("utf-8")) > max_bytes_per_chunk:
                        break
                    temp = next_temp
                    index += 1

                if temp:
                    chunks.append(temp)
                current_chunk = " ".join(words[index:]).strip()
                if not current_chunk:
                    break
        else:
            current_chunk = candidate

    if current_chunk:
        chunks.append(current_chunk)

    return chunks or [""]

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
        self.m_prompttemplate = DEFAULT_LITERATURE_REVIEW_PROMPT
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
            self.m_prompttemplate = DEFAULT_LITERATURE_REVIEW_PROMPT
        else:
            self.m_prompttemplate = LITERATURE_REVIEW_SYSTEM_PROMPT

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
                response = self.preparePrompt(strresearchquestion, strliterature)
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

    def preparePrompt(self, strresearchquestion, strliterature):
        icontextbudget = self.m_cllm.getNumCtx()
        print(f"Context budget token size: {icontextbudget}")
        response = ""
        literature_tokens = self.m_cllm.countTokens(strliterature)
        print(f"Total literature token size: {literature_tokens}")
        literature_blocks = contextBlocks(strliterature, icontextbudget)
        total_blocks = len(literature_blocks)
        for index, strliteratureblock in enumerate(literature_blocks, start=1):
            print(f"Processing literature block {index}/{total_blocks}")
            literatureblock_tokens = self.m_cllm.countTokens(strliteratureblock)
            print(f"literature block token size: {literatureblock_tokens}")
            #print(f"strliteratureblock: {strliteratureblock}")


            response = self.evaluateResearchQuestion(strresearchquestion, strliteratureblock, response)
        # end for
        return response
    # end preparePrompt()

    def evaluateResearchQuestion(self, strresearchquestion, strliterature, strpreviousreview=""):
        prompt_template = self.m_prompttemplate or DEFAULT_LITERATURE_REVIEW_PROMPT

        # Do targeted substitution only in the Runtime Input section.
        # Using str.format() would expand every instructional mention like
        # `{literature}` in the template body, which can duplicate article text
        # many times and explode token usage.
        strprompt = prompt_template

        runtime_replacements = [
            ("Research Questions:\n{research_questions}", f"Research Questions:\n{strresearchquestion}"),
            ("Previous Review Draft (may be empty):\n{review}", f"Previous Review Draft (may be empty):\n{strpreviousreview}"),
            ("Supplied Article Content:\n{literature}", f"Supplied Article Content:\n{strliterature}"),
            ("Article:\n{literature}", f"Article:\n{strliterature}"),
        ]

        for old_text, new_text in runtime_replacements:
            if old_text in strprompt:
                strprompt = strprompt.replace(old_text, new_text, 1)

        # Token accounting by components helps explain unexpectedly large prompt sizes.
        prompt_template_tokens = self.m_cllm.countTokens(prompt_template)
        template_base = (
            prompt_template
            .replace("{research_questions}", "")
            .replace("{literature}", "")
            .replace("{review}", "")
        )
        template_base_tokens = self.m_cllm.countTokens(template_base)
        research_question_tokens = self.m_cllm.countTokens(strresearchquestion)
        prompt_tokens = self.m_cllm.countTokens(strprompt)
        literature_tokens = self.m_cllm.countTokens(strliterature)
        review_tokens = self.m_cllm.countTokens(strpreviousreview)
        estimated_prompt_tokens = (
            template_base_tokens
            + research_question_tokens
            + literature_tokens
            + review_tokens
        )

        response = self.m_cllm.prompt(strprompt)
        response_tokens = self.m_cllm.countTokens(response) if response else 0

        print(f"    Prompt template tokens: {prompt_template_tokens}")
        print(f"    Template base tokens: {template_base_tokens}")
        print(f"    RQ tokens: {research_question_tokens}")
        print(f"    Prompt tokens: {prompt_tokens}")
        print(f"    Estimated prompt tokens: {estimated_prompt_tokens}")
        print(f"    Literature tokens: {literature_tokens}")
        print(f"    Review tokens: {review_tokens}")
        print(f"    Prompt chars: {len(strprompt)}")
        print(f"    Response tokens: {response_tokens}")

        mismatch = abs(prompt_tokens - estimated_prompt_tokens)
        if mismatch > max(estimated_prompt_tokens * 0.15, 300):
            print(
                "    Warning: Prompt token mismatch is high; "
                f"actual={prompt_tokens}, estimated={estimated_prompt_tokens}, delta={mismatch}."
            )

        return response
    # end evaluateResearchQuestion()
# end class CLLM

