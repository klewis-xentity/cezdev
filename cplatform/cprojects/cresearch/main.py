import os
import sys

# Ensure this script's directory and the ccore package root are importable
_HERE = os.path.dirname(os.path.abspath(__file__))
_CCORE = os.path.abspath(os.path.join(_HERE, "..", "..", "..", "ccore"))
for _p in (_HERE, _CCORE):
    if _p not in sys.path:
        sys.path.insert(0, _p)

from cresearch import CResearch
from c3dclasses.ccore.cutility.cutility import readTextFromPDFFilename


def main():
    research = CResearch(1)
    _HERE = os.path.dirname(os.path.abspath(__file__))
    pdfpath = os.path.join(_HERE, "article.pdf")
    print("Opening PDF: " + pdfpath)
    research.addArticle(pdfpath)

    strInfo = "What is the title of the document?"
    print("\n////////////////////////////////\nPrompt: " + strInfo)
    response = research.prompt(strInfo)
    print("\nResponse: " + response)

    print("\n////////////////////////////////")
    print("Ask questions about the document. Type 'quit' or 'exit' to stop.")
    while True:
        print("\nRemaining prompt size:", research.getRemainingContextSize(), "tokens")
        strInfo = input("\nPrompt: ").strip()
        if strInfo.lower() in ("quit", "exit", "q"):
            print("Goodbye.")
            break
        if not strInfo:
            continue
        response = research.prompt(strInfo)
        print("\nResponse: " + response)


if __name__ == "__main__":
    main()
