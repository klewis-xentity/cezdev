import importlib.util
import os

MODULE_PATH = os.path.join(os.path.dirname(__file__), "cutility.py")
spec = importlib.util.spec_from_file_location("cutility_local", MODULE_PATH)
cutility = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cutility)
text2id = cutility.text2id


def test_text2id():
    samples = [
        "hi kevin",
        "hello world",
        "hello",
        "",
    ]

    for sample in samples:
        value = text2id(sample)
        print(f"{sample!r} -> {value} (len={len(value)})")
        assert value.isalnum(), f"'{value}' contains non-alphanumeric characters"
        assert value.isupper(), f"'{value}' is not uppercase"
        assert len(value) == 12, f"'{value}' length is {len(value)}, expected 12"
        assert value == text2id(sample), f"'{value}' is not deterministic"

    print("text2id tests passed")


if __name__ == "__main__":
    test_text2id()
