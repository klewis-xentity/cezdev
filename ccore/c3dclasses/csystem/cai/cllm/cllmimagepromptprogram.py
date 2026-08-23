#---------------------------------------------------------------
# file: cllmimageprogram.py
# desc: program that sends an image and a prompt to a local
#       Ollama vision LLM and prints the response
#---------------------------------------------------------------

import os
from c3dclasses.csystem.cai.cllm.cllm import CLLM

def main():
    cllm = CLLM()
    cllm.useOllama("qwen2.5vl:3b")
    cllm.setTemperature(0.4)
    cllm.m_imagenumctx = 4096

    strpathimages = os.path.join(os.path.dirname(__file__), "test.png")
    strprompt = "Describe the content of the image and provide a brief analysis."
    response = cllm.promptImage(strprompt, strpathimages)
    print(response)
# end main()

if __name__ == "__main__":
    main()
# end if