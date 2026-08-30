import unittest

from c3dclasses.csystem.cai.cllm.cllmcontext import CLLMContext


class CLLMContextToStringTests(unittest.TestCase):
    def setUp(self):
        CLLMContext.clearGlobalContext()

    def test_to_string_shows_context_window_state(self):
        ctx = CLLMContext().create("chat", 6)
        ctx.addContext("hello world")
        ctx.addContext("again")

        self.assertEqual(
            ctx.toString(),
            "CLLMContext:\n"
            "  name: chat\n"
            "  budget: 6\n"
            "  tokens: 4\n"
            "  entries: 2\n"
            "  full: False\n"
            "  window: chat: hello world chat: again",
        )

    def test_to_string_truncates_window_preview(self):
        ctx = CLLMContext().create("chat", CLLMContext.UNLIMITED_BUDGET)
        ctx.addContext("one two three four five six seven eight nine ten eleven twelve thirteen fourteen")

        self.assertIn("  window: chat: one two three four five six seven eight nine ..... twelve thirteen fourteen", ctx.toString())

    def test_to_string_global_context(self):
        CLLMContext.setGlobalContext("System prompt one two three four five six seven eight nine ten eleven twelve")
        self.assertEqual(
            CLLMContext.toStringGlobalContext(),
            "GlobalContext:\n"
            "  name: GlobalContext\n"
            "  budget: -1\n"
            "  tokens: 12\n"
            "  entries: 1\n"
            "  full: False\n"
            "  window: System prompt one two three four five six seven eight ..... ten eleven twelve",
        )

    def test_str_uses_to_string(self):
        ctx = CLLMContext().create("chat", 3)
        ctx.addContext("one")

        self.assertEqual(str(ctx), ctx.toString())


if __name__ == "__main__":
    unittest.main()