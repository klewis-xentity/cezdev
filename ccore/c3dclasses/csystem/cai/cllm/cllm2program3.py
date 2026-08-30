#--------------------------------------------------------------
# file: cllm2program3.py
# desc: interactive program for CLLM2 with pdf context support.
#       type 'page N' to load page N of the pdf into context.
#--------------------------------------------------------------

import os
import sys

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", ".."))
if REPO_ROOT not in sys.path:
    sys.path.insert(0, REPO_ROOT)

from c3dclasses.csystem.cai.cllm.cllm2 import CLLM2
from c3dclasses.csystem.cai.cllm.cllmcontext import CLLMContext
from c3dclasses.csystem.cai.cllm.cllmconversationcontext import CLLMConversationalContext
from c3dclasses.csystem.cai.cllm.cllmpdfcontext import CLLMPDFContext

def main():
    cllm = CLLM2()
    cllm.useOllama("llama3.1")
    cllm.setTemperature(0.4)

    conversation_context = CLLMConversationalContext().create(
        strassistantname="Assistant",
        inumtokenbudget=500,
    )
    conversation_context.setName("You")

    # Load PDF context from the same directory as this script.
    script_dir = os.path.dirname(os.path.abspath(__file__))
    pdf_path = os.path.join(script_dir, "test.pdf")
    pdf_context = CLLMPDFContext().create(pdf_path, icontextbudget=8000)
    if pdf_context.getPageCount() > 0:
        pdf_context.set(0)
        print(f"PDF loaded: {pdf_path}  ({pdf_context.getPageCount()} page(s))")
        print("  Type 'page N' to switch to page N of the PDF.")
    else:
        print(f"No readable PDF text found at: {pdf_path}")

    CLLMContext.setGlobalContext(
        "You are a concise and helpful research assistant. "
    )

    last_response_confidence = None
    last_response_confidence_reason = None

    print("CLLM2 chat started. Type 'quit' to exit.")
    print("Type 'context' to inspect the current context window state.")
    print("Type 'confidence' to show the last response confidence score.")
    print("Type 'reason' to show the last response confidence reason.")
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

        if user_prompt.lower() == "confidence":
            if last_response_confidence is None:
                print("No confidence score yet. Ask a question first.")
            else:
                print(f"Last response confidence: {last_response_confidence:.3f}")
            continue

        if user_prompt.lower() == "reason":
            if not last_response_confidence_reason:
                print("No confidence reason yet. Ask a question first.")
            else:
                print(f"Last response confidence reason: {last_response_confidence_reason}")
            continue

        if user_prompt.lower() == "context":
            print("Global context:", CLLMContext.toStringGlobalContext())
            print("Conversation context:", conversation_context.toString())
            print("PDF context:", pdf_context.toString())
            continue

        # Handle page switching command.
        if user_prompt.lower().startswith("page "):
            parts = user_prompt.split()
            if len(parts) == 2 and parts[1].lstrip("-").isdigit():
                page_num = int(parts[1])
                pdf_context.set(page_num)
                print(f"PDF context set to page {pdf_context.getCurrentPage()} "
                      f"of {pdf_context.getPageCount()}.")
            else:
                print("Usage: page <number>")
            continue

        try:
            response = cllm.promptCLLMContext(
                user_prompt,
                cllmconversationalcontext=conversation_context,
                cllmcontexts=[pdf_context],
            )
            print("Assistant:", response)
            last_response_confidence = cllm.responseConfidenceScore()
            last_response_confidence_reason = cllm.responseConfidenceReason()
            if last_response_confidence is None:
                print("Confidence: unavailable")
            else:
                print(f"Confidence: {last_response_confidence:.3f}")
            if last_response_confidence_reason:
                print(f"Confidence reason: {last_response_confidence_reason}")
            else:
                print("Confidence reason: unavailable")
        except Exception as exc:
            print(f"LLM call failed: {exc}")
            print("Tip: make sure Ollama is running and the selected model is available.")

    CLLMContext.clearGlobalContext()


if __name__ == "__main__":
    main()
