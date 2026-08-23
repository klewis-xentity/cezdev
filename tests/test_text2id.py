import unittest

from c3dclasses.ccore.cutility.cutility import text2id


class Text2IdTests(unittest.TestCase):
    def test_text2id_is_deterministic_and_alphanumeric(self):
        value = text2id("hi kevin")

        self.assertEqual(value, text2id("hi kevin"))
        self.assertTrue(value.isalnum())
        self.assertTrue(value.isupper())
        self.assertEqual(len(value), 12)
        self.assertNotEqual(value, text2id("hi kevinn"))


if __name__ == "__main__":
    unittest.main()
