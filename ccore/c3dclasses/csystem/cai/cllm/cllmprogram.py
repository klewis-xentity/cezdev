import os
from c3dclasses.cai.cllm import CLLM

# Create an instance of CLLM
cllm = CLLM()

# Test the prompt method with and without images
strprompt = "What is the meaning of life?"
strpathimages = None  # No images for this test

response = cllm.prompt(strprompt, strpathimages)
print(response)

# Test the prompt method with images
strpathimages = "C:/Users/oyole/Desktop/test.png"
response = cllm.prompt(strprompt, strpathimages)
print(response)