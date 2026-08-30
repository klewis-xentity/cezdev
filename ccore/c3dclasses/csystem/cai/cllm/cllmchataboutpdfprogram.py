#---------------------------------------------------------------
# file: cllmchataboutpdfprogram.py
# desc: interactive chat program that answers questions about a
#       PDF using a local Ollama LLM with conversation memory
#---------------------------------------------------------------

import os
import sys

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "..", ".."))
if REPO_ROOT not in sys.path:
    sys.path.insert(0, REPO_ROOT)

from c3dclasses.ccore.cutility.cutility import readTextFromPDFFilename
from c3dclasses.csystem.cai.cllm.cllm import CLLM


def build_pdf_context(cllm, pdf_path):
    pdf_text = readTextFromPDFFilename(pdf_path)
    if not pdf_text or not pdf_text.strip():
        return ""

    pdf_chunks = contextBlocks(pdf_text, icontextbudget=8000)

    print("Loaded PDF content length:", len(pdf_text), "characters")
    #pdf_summary = cllm.compressContent(pdf_text, isummarymaxsize=8000)
    #if pdf_summary:
    #    print("Prepared PDF summary length:", len(pdf_summary), "characters")
    #    return pdf_summary

    return pdf_chunks[0] if pdf_chunks else ""

def contextBlocks(strliterature, icontextbudget):
    """Yield context-sized literature chunks using a conservative token estimate.

    We approximate 1 token ~= 4 UTF-8 bytes and reserve part of the budget for
    instructions, question text, and model output.
    """
    text = str(strliterature or "")
    if not text.strip():
        return [""]

    try:
        context_budget = int(icontextbudget) if icontextbudget is not None else 8000
    except (TypeError, ValueError):
        context_budget = 8000

    if context_budget <= 0:
        context_budget = 8000

    # Keep room for prompt scaffolding + response.
    available_tokens = max(int(context_budget * 0.45), 500)
    max_bytes_per_chunk = max(available_tokens * 4, 2000)

    # Prefer sentence-level chunking for better coherence.
    sentence_parts = [s.strip() for s in text.replace("\r\n", "\n").split("\n") if s.strip()]
    chunks = []
    current_chunk = ""

    for part in sentence_parts:
        candidate = f"{current_chunk}\n\n{part}".strip() if current_chunk else part
        candidate_bytes = len(candidate.encode("utf-8"))
        if current_chunk and candidate_bytes > max_bytes_per_chunk:
            chunks.append(current_chunk)
            current_chunk = part
            # Extremely long single paragraph fallback: split by words.
            while len(current_chunk.encode("utf-8")) > max_bytes_per_chunk:
                words = current_chunk.split()
                if len(words) <= 1:
                    head = current_chunk.encode("utf-8")[:max_bytes_per_chunk].decode("utf-8", errors="ignore")
                    head = head.strip()
                    if head:
                        chunks.append(head)
                    current_chunk = current_chunk[len(head):].strip()
                    if not current_chunk:
                        break
                    continue

                temp = ""
                index = 0
                while index < len(words):
                    next_temp = f"{temp} {words[index]}".strip() if temp else words[index]
                    if len(next_temp.encode("utf-8")) > max_bytes_per_chunk:
                        break
                    temp = next_temp
                    index += 1

                if temp:
                    chunks.append(temp)
                current_chunk = " ".join(words[index:]).strip()
                if not current_chunk:
                    break
        else:
            current_chunk = candidate

    if current_chunk:
        chunks.append(current_chunk)

    return chunks or [""]


def main():
    cllm = CLLM()
    cllm.useOllama("llama3.1")
    cllm.setTemperature(0.0)
    cllm.setNumCtx(12000)

    script_dir = os.path.dirname(os.path.abspath(__file__))
    pdf_path = os.path.join(script_dir, "test.pdf")

    if not os.path.exists(pdf_path):
        print(f"PDF not found: {pdf_path}")
        return

    pdf_context = build_pdf_context(cllm, pdf_path)
    if not pdf_context.strip():
        print(f"No readable text found in PDF: {pdf_path}")
        return

    system_prompt = (
        "You are a helpful assistant that answers only questions about the supplied PDF. "
        "Use the PDF context as your source of truth. If the answer is not in the PDF, say so clearly.\n\n"
        f"PDF context:\n{pdf_context}"
    )
    cllm.enableMemory(system_prompt)
    
    print("system_prompt_len:", len(system_prompt))

    print("CLLM PDF chat started. Type 'quit' to exit.")
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
    # end while


if __name__ == "__main__":
    main()
# end if