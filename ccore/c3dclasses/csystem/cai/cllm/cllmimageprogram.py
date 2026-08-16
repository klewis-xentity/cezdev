import os
from c3dclasses.csystem.cai.cllm.cllm import CLLM

# Create an instance of CLLM
cllm = CLLM()

# Match the settings of the reference ollama.Client program
cllm.useOllama("qwen2.5vl:3b")   # model="qwen2.5vl:3b"
cllm.setTemperature(0.4)         # temperature=0.4
cllm.m_imagenumctx = 4096        # num_ctx=4096 for image prompts

# Test the prompt method with images
strpathimages = "E:/cezdev/ccore/c3dclasses/csystem/cai/cllm/test.png"
strprompt = "Describe the content of the image and provide a brief analysis."
response = cllm.prompt(strprompt, strpathimages)
print(response)