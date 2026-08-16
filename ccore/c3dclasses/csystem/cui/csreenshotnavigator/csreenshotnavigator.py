"""csreenshotnavigator module."""
"""Example:  E:\cezdev\ccore\c3dclasses\csystem\cui\csreenshotnavigator\main.py E:\cezdev\ccore\c3dclasses\csystem\cui\csreenshotnavigator\slack_icon.png"""

import cv2
import easyocr
import mss
import numpy as np
import pyautogui
import pyperclip
import time


class CScreenshotNavigator:
	"""Basic screenshot navigator class."""
	MATCH_THRESHOLD = 0.25

	def __init__(self, ocr_languages=None, use_gpu=False):
		self.m_ocrAvailable = False
		self.m_ocrReader = None
		self.__configureOCR(ocr_languages=ocr_languages, use_gpu=use_gpu)
		self.m_curScreenshot = self.__takeScreenshot()
		self.current_index = 0

	def __takeScreenshot(self):
		with mss.MSS() as sct:
			monitor = sct.monitors[0]
			screenshot = sct.grab(monitor)
			image = np.array(screenshot)
			return cv2.cvtColor(image, cv2.COLOR_BGRA2BGR)

	def clickImage(self, image_path):
		if image_path is None:
			raise ValueError("'image_path' must be provided.")

		self.m_curScreenshot = self.__takeScreenshot()
		template = cv2.imread(str(image_path), cv2.IMREAD_COLOR)
		if template is None:
			raise FileNotFoundError(f"Could not read template image: {image_path}")

		result = cv2.matchTemplate(self.m_curScreenshot, template, cv2.TM_CCOEFF_NORMED)
		_, max_value, _, max_location = cv2.minMaxLoc(result)

		if max_value >= self.MATCH_THRESHOLD:
			template_height, template_width = template.shape[:2]
			center_x = max_location[0] + template_width // 2
			center_y = max_location[1] + template_height // 2

			pyautogui.moveTo(center_x, center_y, duration=0.2)
			pyautogui.click()
			return True

		return False

	def clickText(self, text):
		if text is None or not str(text).strip():
			raise ValueError("'text' must be a non-empty string.")

		if not self.m_ocrAvailable:
			return False

		self.m_curScreenshot = self.__takeScreenshot()
		text_center = self.__findTextCenterOnScreenshot(text)
		if text_center is not None:
			pyautogui.moveTo(text_center[0], text_center[1], duration=0.2)
			pyautogui.click()
			return True

		return False

	def wait_until(self, image=None, text=None, numberofsecond=10, numberofscreenshot=None):
		if image is None and text is None:
			raise ValueError("At least one of 'image' or 'text' must be provided.")

		deadline = time.time() + float(numberofsecond)
		screenshot_count = 0

		while time.time() <= deadline:
			if numberofscreenshot is not None and screenshot_count >= int(numberofscreenshot):
				return False

			self.m_curScreenshot = self.__takeScreenshot()
			screenshot_count += 1

			if image is not None and self.__isImageVisible(image):
				return True

			if text is not None and self.__isTextVisibleOnScreenshot(text):
				return True

			time.sleep(0.2)

		return False

	def getCurrentScreenshot(self):
		return self.m_curScreenshot

	def selectAllText(self):
		for _ in range(3):
			sentinel = f"__CSN_CLIPBOARD_SENTINEL__{time.time_ns()}"
			pyperclip.copy(sentinel)
			pyautogui.hotkey("ctrl", "a")
			time.sleep(0.05)
			pyautogui.hotkey("ctrl", "c")

			deadline = time.time() + 1.0
			while time.time() <= deadline:
				copied_text = self.__getClipboardText()
				if copied_text != sentinel:
					return copied_text
				time.sleep(0.05)

		return ""

	def copyToClipboard(self, text):
		pyperclip.copy("")
		pyperclip.copy("" if text is None else str(text))

	def __getClipboardText(self):
		try:
			return pyperclip.paste()
		except Exception:
			return ""

	def __isImageVisible(self, image_path):
		template = cv2.imread(str(image_path), cv2.IMREAD_COLOR)
		if template is None:
			raise FileNotFoundError(f"Could not read template image: {image_path}")

		result = cv2.matchTemplate(self.m_curScreenshot, template, cv2.TM_CCOEFF_NORMED)
		_, max_value, _, _ = cv2.minMaxLoc(result)
		return max_value >= self.MATCH_THRESHOLD

	def __isTextVisibleOnScreenshot(self, text):
		return self.__findTextCenterOnScreenshot(text) is not None

	def __findTextCenterOnScreenshot(self, text):
		if not self.m_ocrAvailable or self.m_ocrReader is None:
			return None

		if self.m_curScreenshot is None:
			return None

		target = str(text).strip().lower()
		if not target:
			return None

		rgb_image = cv2.cvtColor(self.m_curScreenshot, cv2.COLOR_BGR2RGB)
		try:
			results = self.m_ocrReader.readtext(rgb_image)
		except Exception:
			self.m_ocrAvailable = False
			self.m_ocrReader = None
			return None

		for box, detected_text, _confidence in results:
			candidate = str(detected_text).strip().lower()
			if not candidate:
				continue
			if target not in candidate:
				continue

			x_values = [point[0] for point in box]
			y_values = [point[1] for point in box]
			center_x = int(sum(x_values) / len(x_values))
			center_y = int(sum(y_values) / len(y_values))
			return (center_x, center_y)

		return None

	def __configureOCR(self, ocr_languages=None, use_gpu=False):
		languages = ocr_languages if ocr_languages else ["en"]
		try:
			self.m_ocrReader = easyocr.Reader(languages, gpu=use_gpu)
			self.m_ocrAvailable = True
		except Exception:
			self.m_ocrReader = None
			self.m_ocrAvailable = False
		return self.m_ocrAvailable

