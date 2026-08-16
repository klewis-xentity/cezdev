from cresearch import CResearch

def main():
    print("Starting CResearch test...")
    research = CResearch(1)
    #response = research.prompt("Hello, can you summarize the main points of the article titled 'Am I Wrong, or Is the Autograder Wrong? Effects of AI Grading Mistakes on Learning' by Tiffany Wenting Li et al.?")
    #print("\nResponse: " + response)

#    _HERE = os.path.dirname(os.path.abspath(__file__))
#    pdfpath = os.path.join(_HERE, "article.pdf")
#    print("Opening PDF: " + pdfpath)
#    research.addArticle(pdfpath)

#    strInfo = "What is the title of the document?"
#    print("\n////////////////////////////////\nPrompt: " + strInfo)
#    response = research.prompt(strInfo)
#    print("\nResponse: " + response)

#    print("\n////////////////////////////////")
#    print("Ask questions about the document. Type 'quit' or 'exit' to stop.")
    while True:
        print("\nRemaining prompt size:", research.getRemainingContextSize(), "tokens")
        strInfo = input("\nPrompt: ").strip()
        if strInfo.lower() in ("quit", "exit", "q"):
            print("Goodbye.")
            break
        if not strInfo:
            continue
        response = research.prompt(strInfo)
        print("\nResponse: " + response)


if __name__ == "__main__":
    main()
