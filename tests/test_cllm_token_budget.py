import unittest

from c3dclasses.csystem.cai.cllm.cllm import CLLM


class CLLMTokenBudgetTests(unittest.TestCase):
    def test_remaining_tokens_uses_context_budget(self):
        cllm = CLLM()
        cllm.setNumCtx(100)
        cllm.enableMemory("system prompt")

        self.assertEqual(cllm.getContextTokenSize(), 100)
        expected_remaining = cllm.getContextTokenSize() - (
            cllm.getHistoryTokenSize() + cllm.getSystemPromptTokenSize()
        )
        self.assertEqual(cllm.getRemainingTokens(), max(expected_remaining, 0))

        cllm.m_history.append(("user", "hello"))
        expected_remaining = cllm.getContextTokenSize() - (
            cllm.getHistoryTokenSize() + cllm.getSystemPromptTokenSize()
        )
        self.assertEqual(cllm.getRemainingTokens(), max(expected_remaining, 0))


if __name__ == "__main__":
    unittest.main()
