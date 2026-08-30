    #--------------------------------------------------------------
    # parsing methods
    #--------------------------------------------------------------    
    def parseAllCode(self):
        logger.info("Success: CLLM :: parseAllCode() - Parsing all code blocks from prompt response.")
        if self.m_strpromptresponse:
            code_pattern = r'```(.*?)```'
            code_matches = re.findall(code_pattern, self.m_strpromptresponse, re.DOTALL)
            logger.debug(f"Success: CLLM :: parseAllCode() - Code blocks found: {code_matches}")
            return [code_block.strip() for code_block in code_matches]
        return None    
    # end parseAllCode()
    
    def parseCode(self, strtype):
        logger.info(f"Success: CLLM :: parseCode() - Parsing specific code of type: {strtype}")
        allcode = self.parseAllCode()
        if allcode:
            for code in allcode: 
                if strtype in code:
                    return code.replace(strtype, "").strip()
            return allcode[0].strip()
        return "" 
    # end parseCode()
 
    def parseJSON(self):
        try:
            return json.loads(self.parseCode("json"))
        except json.JSONDecodeError as e:
            logger.error(f"Failure: CLLM :: parseJSON() - Error parsing JSON: {e}")
            return None
    # end parseJSON()
    
    
    def summarize(self, text, strconstraints, max_chunk_size=3000, summary_length=150):
        """
        Summarize a large body of text using an LLM.

        Parameters:
            text (str): The large text to summarize.
            model (str): The model to use for summarization.
            max_chunk_size (int): Maximum character limit per chunk.
            summary_length (int): Approximate length of each summary in words.

        Returns:
            str: A cohesive summary of the text.
        """
        # Split the text into manageable chunks
        chunks = []
        while len(text) > max_chunk_size:
            # Split at the last sentence within the chunk size limit
            split_point = text[:max_chunk_size].rfind(". ")
            if split_point == -1:
                split_point = max_chunk_size
            chunks.append(text[:split_point + 1])
            text = text[split_point + 1:]
        chunks.append(text)

        # Summarize each chunk
        summaries = []
        for i, chunk in enumerate(chunks):
            print(f"Success: CLLM :: summarize() - Processing chunk {i + 1} of {len(chunks)}...")
            prompt = (
                f"Summarize the following java code in approximately {summary_length} words.\n\nContraints of Summary:\n{strconstraints}\n\nChunk of Text to Summerize\n{chunk}"
            )
            try:
                summary = self._prompt(prompt)
                summaries.append(summary.strip())
            except Exception as e:
                print(f"Failure: CLLM :: summarize() - Error summarizing chunk {i + 1}: {e}")
                summaries.append("")
        # end fof
        
        # Combine the chunk summaries
        final_summary_prompt = (
            "Combine the following summaries into a cohesive overall summary:\n\n"
            + "\n\n".join(summaries)
        )
        try:
            self.setMaxTokens(summary_length * 4),
            final_summary = self._prompt(final_summary_prompt)
        except Exception as e:
            print(f"Failure: CLLM :: summarize() - Error generating final summary: {e}")
            final_summary = " ".join(summaries)  # Fallback to concatenated summaries
        return final_summary
    # end summarize_large_text()