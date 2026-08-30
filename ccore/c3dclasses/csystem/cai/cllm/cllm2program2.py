#--------------------------------------------------------------
# file: cllm2program2.py
# desc: interactive program for CLLM2 (non-test usage)
#--------------------------------------------------------------

try:
    from c3dclasses.csystem.cai.cllm.cllm2 import CLLM2
    from c3dclasses.csystem.cai.cllm.cllmcontext import CLLMContext
    from c3dclasses.csystem.cai.cllm.cllmconversationcontext import CLLMConversationalContext
except ImportError as exc:
    raise SystemExit(
        "Unable to import CLLM2. Run this program with the project root on PYTHONPATH, "
        "for example: set PYTHONPATH=e:\\cezdev\\ccore"
    ) from exc


def main():
    cllm = CLLM2()
    cllm.useOllama("llama3.1")
    cllm.setTemperature(0.4)
    conversation_context = CLLMConversationalContext().create(
        strassistantname="Assistant",
        inumtokenbudget=50,
    )
    conversation_context.setName("You")

    CLLMContext.setGlobalContext("You are a concise and helpful assistant who know just basketball and nothing else. You are not allowed to answer questions about anything else. You are not allowed to answer questions")

    print("CLLM2 chat started. Type 'quit' to exit.")
    while True:
        try:
            user_prompt = input("You: ").strip()
        except KeyboardInterrupt:
            print("\nExiting chat...")
            break

        if not user_prompt:
            continue

        if user_prompt.lower() in {"quit", "exit", "bye"}:
            print("Exiting chat...")
            break

        try:
            response = cllm.promptCLLMContext(
                user_prompt,
                cllmconversationalcontext=conversation_context,
            )
            print("Assistant:", response)
        except Exception as exc:
            print(f"LLM call failed: {exc}")
            print("Tip: make sure Ollama is running and the selected model is available.")

    CLLMContext.clearGlobalContext()


if __name__ == "__main__":
    main()
