#-----------------------------------------------------------------
# file: cllmcompresspromptprogram.py
# desc: program that compresses a prompt to a target token budget
#       using a local Ollama LLM
#-----------------------------------------------------------------

from c3dclasses.csystem.cai.cllm.cllm import CLLM

def main():
    cllm = CLLM()
    cllm.useOllama("llama3.1")
    cllm.setTemperature(0.4)

    original_prompt = (
        f"Please review the following meeting notes and create a concise action plan. "
        f"Preserve all deadlines, owner names, priorities, dependencies, and any blockers. "
        f"Format the response as a numbered list with one item per action and include a brief risk summary at the end. "
        "Meeting notes: John owns API update by Friday, {article-goes-here} Sara owns UI polish by next Tuesday, "
        f"DevOps must provision staging before UI testing can begin, and security review is required before release."
    )
    target_tokens = 100

    compressed_prompt = cllm.compressPrompt(original_prompt, target_tokens)

    print("Original Prompt:")
    print(original_prompt)
    print("\nCompressed Prompt:")
    print(compressed_prompt)

    print("\nEstimated Tokens:")
    print(f"Original: {cllm._estimate_tokens(original_prompt)}")
    print(f"Compressed: {cllm._estimate_tokens(compressed_prompt)}")
    print(f"Target: {target_tokens}")
    
# end main()

if __name__ == "__main__":
    main()
# end if
