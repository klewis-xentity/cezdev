import sys
import tkinter as tk
from tkinter import messagebox

def show_alert(message):
    root = tk.Tk()
    root.withdraw()  # Hide main window
    messagebox.showinfo("Alert", message)
    root.destroy()

if __name__ == "__main__":
    if len(sys.argv) > 1:
        # Join all command line arguments as a single message string
        message = " ".join(sys.argv[1:])
    else:
        message = "No message provided!"

    show_alert(message)
