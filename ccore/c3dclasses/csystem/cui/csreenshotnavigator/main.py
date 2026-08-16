"""Manual test runner for CScreenshotNavigator."""
"""Usage: python main.py <image_path> or python main.py --text \"Submit\""""
"""Example: python main.py --text \"Kevin Lewis\" --use-gpu"""

import argparse
import time

try:
	from c3dclasses.csystem.cui.csreenshotnavigator.csreenshotnavigator import CScreenshotNavigator
except ModuleNotFoundError:
	from csreenshotnavigator import CScreenshotNavigator


def main():
	parser = argparse.ArgumentParser(
		description="Locate and click an image or text on screen using CScreenshotNavigator."
	)
	parser.add_argument(
		"image_path",
		nargs="?",
		help="Path to template image to find on screen.",
	)
	parser.add_argument(
		"--text",
		dest="target_text",
		help="Text to find on screen and click using OCR.",
	)
	parser.add_argument(
		"--use-gpu",
		action="store_true",
		help="Enable GPU for EasyOCR if available.",
	)
	parser.add_argument(
		"--post-click-wait",
		type=float,
		default=1.0,
		help="Seconds to wait after clicking image before selecting text.",
	)
	args = parser.parse_args()

	navigator = CScreenshotNavigator(use_gpu=args.use_gpu)
	if not navigator.m_ocrAvailable:
		print("Warning: EasyOCR is not available. Text detection/clicking is disabled.")
		print("Install EasyOCR: python -m pip install easyocr")

	if args.target_text and not navigator.m_ocrAvailable:
		print("Cannot click text without EasyOCR.")
		return

	if args.target_text:
		clicked = navigator.clickText(text=args.target_text)
		if not clicked:
			print(f"Text not found: {args.target_text}")
			return
		print(f"Clicked text: {args.target_text}")
		return

	if not args.image_path:
		print("Provide either <image_path> or --text.")
		return

	clicked = navigator.clickImage(image_path=args.image_path)
	if not clicked:
		print("Image not found (below match threshold).")
		return

	time.sleep(max(0.0, args.post_click_wait))

	print("Image found and clicked.")

	clicked = navigator.clickText(text="You")
	if not clicked:
		print("Text not found: Kevin Lewis You")
		return
	
	print("Clicked text: Kevin Lewis You")

	if not navigator.wait_until(text="Kevin Lewis", numberofsecond=5, numberofscreenshot=3):
		print("Text not found before timeout/screenshot limit.")
		return
	
	print("Text found before timeout/screenshot limit.")
	
	#if not navigator.clickImage(image_path=args.image_path):
	#	print("Text was detected but could not be clicked.")
	#	return

	selected_text = navigator.selectAllText()
	print("Selected text:")
	if selected_text:
		print(selected_text)
	else:
		print("<empty>")
		print("No text captured. Ensure the target window is focused and supports Ctrl+A/Ctrl+C.")


if __name__ == "__main__":
	main()
