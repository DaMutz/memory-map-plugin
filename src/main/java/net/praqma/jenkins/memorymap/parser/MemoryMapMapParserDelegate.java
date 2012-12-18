/*
 * The MIT License
 *
 * Copyright 2012 Praqma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins.memorymap.parser;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.praqma.jenkins.memorymap.result.MemoryMapParsingResult;
import org.apache.tools.ant.types.FileSet;

/**
 * Class to wrap the FileCallable method. Serves as a proxy to the parser method. 
 * @author Praqma
 */
public class MemoryMapMapParserDelegate implements FilePath.FileCallable<List<MemoryMapParsingResult>> 
{
    private static final Logger log = Logger.getLogger(MemoryMapMapParserDelegate.class.getName());
    private AbstractMemoryMapParser parser;
    //Empty constructor. For serialization purposes.
    public MemoryMapMapParserDelegate() { }

    public MemoryMapMapParserDelegate(AbstractMemoryMapParser parser) {
        this.parser = parser;
    }

    @Override
    public List<MemoryMapParsingResult> invoke(File file, VirtualChannel vc) throws IOException, InterruptedException {        

        try {
            return getParser().parseMapFile(findFile(file));
        } catch (FileNotFoundException fnfex) {
            log.logp(Level.WARNING, "invoke", MemoryMapConfigFileParserDelegate.class.getName(), "incvok caught file not found exception", fnfex);
            throw new IOException(fnfex.getMessage());
        }
    }

    /**
     * @return the parser
     */
    public AbstractMemoryMapParser getParser() {
        return parser;
    }

    /**
     * @param parser the parser to set
     */
    public void setParser(AbstractMemoryMapParser parser) {
        this.parser = parser;
    }
    
    public File findFile(File file) throws IOException {
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
        fileSet.setProject(project);
        fileSet.setDir(file);
        fileSet.setIncludes(parser.getMapFile());
        
        int numberOfFoundFiles = fileSet.getDirectoryScanner(project).getIncludedFiles().length;
        if(numberOfFoundFiles == 0) {
            throw new FileNotFoundException(String.format("Filematcher found no files using pattern %s in folder %s",parser.getMapFile(),file.getAbsolutePath()));
        } 
        
        File f = new File(file.getAbsoluteFile() + System.getProperty("file.separator") + fileSet.getDirectoryScanner(project).getIncludedFiles()[0]);

        if(!f.exists()) {
            throw new FileNotFoundException(String.format("File %s not found workspace was %s scanner found %s files.", f.getAbsolutePath(),file.getAbsolutePath(),numberOfFoundFiles));
        }
        return f;
    }

}