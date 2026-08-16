from c3dclasses.csystem.cai.cllm.cllm import CLLM
from c3dclasses.ccore.cutility.cutility import readTextFromPDFFilename

def main():
    
    
    cllm = CLLM()
    cllm.useOllama("llama3.1")
    cllm.setTemperature(0.4)
    
    strcontentfilename = "E://cezdev//ccore//c3dclasses//csystem//cai//cllm//test.pdf"
    strcontenttosummarize = readTextFromPDFFilename(strcontentfilename)
    

    print("Original Content Length to Summarize and Compress:", len(
        strcontenttosummarize), "characters")

    strcompressedcontent = cllm.compressContent(strcontenttosummarize)
    print("Compressed Content Length:", len(strcompressedcontent), "characters")
    print("\nCompressed Content:")
    print(strcompressedcontent)

# end main()

if __name__ == "__main__":
    main()
# end if
