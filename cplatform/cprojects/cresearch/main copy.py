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
    #pdftext = readTextFromPDFFilename("document.pdf")
    #response = research.prompt(pdftext)
    strInfo = "My name is Kevin and I live in New York. I am a software engineer with 5 years of experience in web development. and live at 801 Riverside Drive, New York, NY 10032. I have a passion for creating efficient and scalable web applications. I am proficient in Python, JavaScript, and React. In my free time, I enjoy hiking and exploring new technologies. I like to run long distances and have completed several marathons. I am also an avid reader and enjoy learning about new programming languages and frameworks."
    print("Prompt: " + strInfo)
    response = research.prompt(strInfo)
    print("\nResponse: " + response)
    
    strInfo = "What is my name and where do I live?"
    print("\n////////////////////////////////\nPrompt: " + strInfo)
    response = research.prompt(strInfo)
    print("\nResponse: " + response)
    
    strInfo = "What do I like to do in my free time?"
    print("\n////////////////////////////////\nPrompt: " + strInfo)
    response = research.prompt(strInfo)
    print("\nResponse: " + response)
    



if __name__ == "__main__":
    main()
