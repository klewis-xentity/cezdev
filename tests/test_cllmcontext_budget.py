import unittest

from c3dclasses.csystem.cai.cllm.cllmcontext import CLLMContext


class CLLMContextBudgetTests(unittest.TestCase):
    def test_trims_window_by_total_tokens(self):
        ctx = CLLMContext().create("ctx", 5)

        ctx.setContext("ctx", "one two")
        ctx.setContext("ctx", "three four five")
        self.assertEqual(ctx.getContext(), ["one two", "three four five"])

        ctx.setContext("ctx", "six")
        self.assertEqual(ctx.getContext(), ["three four five", "six"])

    def test_accepts_numeric_token_entries(self):
        ctx = CLLMContext().create("ctx", 5)

        ctx.setContext("ctx", 3)
        ctx.setContext("ctx", 2)
        self.assertTrue(ctx.isFullContext("ctx"))

        ctx.setContext("ctx", 2)
        self.assertEqual(ctx.getContext(), [2, 2])

    def test_is_full_context_uses_total_tokens(self):
        ctx = CLLMContext().create("ctx", 5)

        ctx.setContext("ctx", "one two")
        ctx.setContext("ctx", "three four")
        self.assertFalse(ctx.isFullContext("ctx"))

        ctx.setContext("ctx", "five")
        self.assertTrue(ctx.isFullContext("ctx"))


if __name__ == "__main__":
    unittest.main()
