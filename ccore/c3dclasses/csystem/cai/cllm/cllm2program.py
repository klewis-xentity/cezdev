#--------------------------------------------------------------
# file: cllm2program.py
# desc: runnable smoke test for the CLLM2 class without calling
#       a live LLM endpoint
#--------------------------------------------------------------

try:
    from c3dclasses.csystem.cai.cllm.cllm2 import CLLM2
    from c3dclasses.csystem.cai.cllm.cllmcontext import CLLMContext
except ImportError as exc:
    raise SystemExit(
        "Unable to import CLLM2. Run this program with the project root on PYTHONPATH, "
        "for example: set PYTHONPATH=e:\\cezdev\\ccore"
    ) from exc

class FakeCLLM2(CLLM2):
    def _prompt(self, strprompt):
        if strprompt.startswith("Format the following prompt as bullet list:"):
            return "- rewritten\n- prompt"
        # end if

        if strprompt.startswith("Modify the following prompt based on these constraints:"):
            return "Shortened prompt with the requested constraints."
        # end if

        if strprompt.startswith("Compress the following prompt to fit within"):
            return "Compressed prompt"
        # end if

        if strprompt.startswith("Validate the following response to the prompt:"):
            if "Response: valid answer" in strprompt:
                return "yes"
            # end if
            return "no"
        # end if

        return f"echo::{strprompt}"
    # end _prompt()
# end FakeCLLM2

def assert_equal(actual, expected, label):
    if actual != expected:
        raise AssertionError(f"{label} failed: expected {expected!r}, got {actual!r}")
    # end if
    print(f"PASS: {label}")
# end assert_equal()

def main():
    cllm = FakeCLLM2()

    CLLMContext.setGlobalContext("System: shared global context")
    assert_equal(CLLMContext.getGlobalContext(), "System: shared global context", "global context is stored statically")

    response = cllm.prompt("hello world")
    assert_equal(response, "echo::hello world", "prompt returns backend response")
    assert_equal(cllm.getPrompt(), "hello world", "getPrompt stores last prompt")
    assert_equal(cllm.getPromptResponse(), "echo::hello world", "getPromptResponse stores last response")

    formatted = cllm.promptWithFormat("hello world", "bullet list")
    assert_equal(formatted, "- rewritten\n- prompt", "promptWithFormat applies requested format")

    replayed = cllm.promptAgain()
    assert_equal(replayed, "echo::hello world", "promptAgain resends the last raw prompt")

    modified = cllm.promptModify("Write a long answer", "Make it brief")
    assert_equal(modified, "Shortened prompt with the requested constraints.", "promptModify supports constraint-only calls")

    compressed = cllm.promptModify("Write a long answer", 25, "Keep the key facts")
    assert_equal(compressed, "Compressed prompt", "promptModify supports token budget calls")

    prompt_with_global_context = cllm.promptCLLMContext("final question")
    assert_equal(prompt_with_global_context, "echo::System: shared global context\nfinal question", "promptCLLMContext prepends shared global context")

    cllm.m_strpromptresponse = "yes"
    assert_equal(cllm.responseToBoolean(), True, "responseToBoolean parses yes")

    cllm.m_strpromptresponse = "42"
    assert_equal(cllm.responseToInteger(), 42, "responseToInteger parses integers")

    cllm.m_strpromptresponse = "3.14"
    assert_equal(cllm.responseToFloat(), 3.14, "responseToFloat parses floats")

    cllm.m_strprompt = "Check this"
    cllm.m_strpromptresponse = "valid answer"
    assert_equal(cllm.isResponseValid(), True, "isResponseValid accepts approved responses")

    cllm.m_strpromptresponse = "hallucinated answer"
    assert_equal(cllm.isResponseDelusional(), True, "isResponseDelusional flips validation result")

    CLLMContext.clearGlobalContext()
    assert_equal(CLLMContext.getGlobalContext(), "", "clearGlobalContext resets shared context")

    print("CLLM2 smoke test completed successfully.")
# end main()

if __name__ == "__main__":
    main()
# end if