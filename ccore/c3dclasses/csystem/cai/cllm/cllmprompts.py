#---------------------------------------------------
# file: cllmprompts.py
# desc: central prompt templates used by CLLM2
#---------------------------------------------------

DEFAULT_SUMMARIZE_PROMPT = """
You are a high-fidelity summarizer. Your job is to preserve the original meaning, intent, and important details of the source content while making it concise and readable.

Summarize the content faithfully enough that a reader still understands:
- the main idea and purpose
- the most important facts, claims, arguments, and evidence
- key people, names, dates, numbers, and entities
- causal relationships, trade-offs, constraints, risks, and conclusions
- any actionable instructions or decisions that matter

Rules:
1. Do not invent, soften, or distort meaning.
2. Do not drop important details simply to save space.
3. Keep the original intent, tone, and emphasis.
4. Prefer accuracy over brevity when nuance matters.
5. Remove redundancy, not substance.
6. Preserve critical context, exceptions, and qualifiers.
7. Output only the final summary text, with no commentary or preamble.

Preferred style:
- concise but complete
- logically ordered
- easy to understand
- keeps the essential details even if the summary is longer than a generic summary
- if the content is complex, use a short structure like: main idea, supporting details, implications, and action items when relevant
"""

PROMPT_WITH_FORMAT_TEMPLATE = "Format the following prompt as {output_format}:\n\n{prompt}"
PROMPT_MODIFY_TEMPLATE = "Modify the following prompt based on these constraints:\n\nPrompt: {prompt}\nConstraints: {constraints}"
PROMPT_COMPRESS_TEMPLATE = "Compress the following prompt to fit within {tokens} tokens:\n\nPrompt: {prompt}\nConstraints: {constraints}"
PROMPT_RESPONSE_OBJECT_TEMPLATE = "Format the following response as {output_format}:\n\nResponse: {response}"
PROMPT_RESPONSE_VALIDATION_TEMPLATE = "Validate the following response to the prompt:\n\nPrompt: {prompt}\nResponse: {response}\nIs the response valid? (yes/no)"

PROMPT_CONFIDENCE_SCORE_TEMPLATE = (
    "Rate the confidence of the following response to the prompt on a scale of 0 to 1. "
    "Return only a single numeric value in [0,1] with no words.\n\n"
    "Prompt: {prompt}\n"
    "Response: {response}\n"
    "Confidence:"
)

PROMPT_CONFIDENCE_REASON_TEMPLATE = (
    "Explain why the confidence score for the following response should be what it is. "
    "Return a concise reason in 1-3 sentences with no score value.\n\n"
    "Prompt: {prompt}\n"
    "Response: {response}\n"
    "Reason:"
)
