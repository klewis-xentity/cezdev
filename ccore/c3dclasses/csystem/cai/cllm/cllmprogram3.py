import os
from c3dclasses.csystem.cai.cllm.cllm import CLLM

# Create an instance of CLLM
cllm = CLLM()

# Match the settings of the reference ollama.Client program
cllm.useOllama("qwen2.5vl:3b")   # model="qwen2.5vl:3b"
cllm.setTemperature(0.4)         # temperature=0.4

cllm.setNumCtxByK(4)  # num_ctx=4096 for image prompts

# Test the prompt method with and without images
strprompt = "What does the image depict?"
#strpathimages = None  # No images for this test

#response = cllm.prompt(strprompt, strpathimages)
#print(response)

# Test the prompt method with images
strpathimages = "C:/Users/oyole/Desktop/test.png"
response = cllm.prompt(strprompt, strpathimages)
print(response)