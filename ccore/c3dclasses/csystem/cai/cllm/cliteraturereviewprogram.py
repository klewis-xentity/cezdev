#---------------------------------------------------------------
# file: cliteraturereviewprogram.py
# desc: simple literature review example with one article and one question
#---------------------------------------------------------------

import os
import sys

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "..", ".."))
if REPO_ROOT not in sys.path:
    sys.path.insert(0, REPO_ROOT)

from c3dclasses.csystem.cai.cllm.cllm import CLLM
from c3dclasses.csystem.cai.cllm.cliteraturereview import CLiteratureReview


article = """
This paper studies the effect of adaptive learning tools on student motivation.
The authors tested a tool with 180 students over one semester. Students using the tool
reported higher engagement and more consistent homework completion than students in a
traditional classroom. The study also noted that the tool was most helpful for students
who started with lower confidence levels. The authors suggest the tool may improve
motivation, but they also note that the study was limited to one school and used
self-reported surveys.
"""

question = "What does the article say about the effect of adaptive learning tools on student motivation?"


def main():
    cllm = CLLM()
    cllm.useOllama("llama3.1")
    cllm.setTemperature(0.2)
    cllm.setNumCtx(12000)

    script_dir = os.path.dirname(os.path.abspath(__file__))
    pdf_path = os.path.join(script_dir, "test.pdf")
    pdf_path2 = os.path.join(script_dir, "test2.pdf")
    review_path = os.path.join(script_dir, "literature_review.json")

    reviewer = CLiteratureReview()
    reviewer.create(strreviewfilename=review_path, cllm=cllm)
    reviewer.useDefaultPrompts(bUseDefaultPrompts=False)
    reviewer.addResearchQuestion(question)
    reviewer.addLiteratureFilename(pdf_path)
    reviewer.addLiteratureFilename(pdf_path2)
    
    reviewer.reviewLiterature()


if __name__ == "__main__":
    main()
