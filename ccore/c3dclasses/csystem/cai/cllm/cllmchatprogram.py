from c3dclasses.csystem.cai.cllm.cllm import CLLM


def main():
    cllm = CLLM()
    cllm.useOllama("llama3.1")
    cllm.setTemperature(0.4)
    cllm.enableMemory("You are a helpful assistant. That can only answer questions about basketball and nothing else. You are not allowed to answer questions about anything else. You are not allowed to answer questions about anything else. You are not allowed to answer questions about")

    print("CLLM chat started. Type 'quit' to exit.")

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

        response = cllm.prompt(user_prompt)
        print("Assistant:", response)
        #print(f"[tokens] used={cllm.getHistoryTokenSize() + cllm.getSystemPromptTokenSize()} remaining={cllm.getRemainingTokens()} max={cllm.getContextTokenSize()}")
        #print(
        #    f"[durations] prompt_eval={cllm.getPromptEvalDuration() / 1000000000:.3f}s "
        #    f"eval={cllm.getEvalDuration() / 1000000000:.3f}s total={cllm.getTotalDuration() / 1000000000:.3f}s"
        #)


if __name__ == "__main__":
    main()
