import os
import tempfile
import unittest

from c3dclasses.ccore.cutility.cutility import readJSONFromFilename


class JSONFileLoadTests(unittest.TestCase):
    def test_read_json_from_filename_returns_empty_review_for_invalid_json_file(self):
        fd, path = tempfile.mkstemp(suffix='.json')
        os.close(fd)
        try:
            with open(path, 'wb') as outfile:
                outfile.write(b'\x9d\x00')

            self.assertEqual(readJSONFromFilename(path), {
                "articles": {},
                "researchquestions": {},
                "reviews": {}
            })
        finally:
            if os.path.exists(path):
                os.remove(path)


if __name__ == '__main__':
    unittest.main()
