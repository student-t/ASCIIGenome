package commandHelp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import exceptions.InvalidCommandLineException;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

public class CommandList {
	
	private static String SEE_ALSO= "\nFull documentation at: http://asciigenome.readthedocs.io/\n\n";
	
	public static ConsoleReader initConsole() throws IOException{
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		    	System.out.print("\033[0m"); // On exit turn off all formatting
		    }
		}));
		
		ConsoleReader console= new ConsoleReader(); 
		try {
			for(CommandHelp x : CommandList.commandHelpList()){
				if(x.getName().length() > 2){
					console.addCompleter(new StringsCompleter(x.getName()));
				}
			}
		} catch (InvalidCommandLineException e) {
			e.printStackTrace();
		}
		return console;
	}
	
	private static String reStructuredTextHelp() throws InvalidCommandLineException{

		String intro = ""
				+ ".. This document is autogenerated by CommandList.reStructuredTextHelp().\n"
				+ "   Do not edit it here. Edit source code then run tests in CommandListTest.updateReStructuredFile() to recreate "
				+ "this file.\n\n";

		intro += ""
				+ "Command reference\n"
         		+ "=================\n\n";
		intro += "This is the documentation for the indvidual commands. "
				+ "The help documented here can be invoked also at the command prompt with `command -h`, for example to "
				+ "get the help for `ylim`::\n"
				+ "\n"
				+ "    ylim -h\n"
				+ "\n"
				+ "Parameters in square brakets are optional and the default argument is "
				+ "indicated by the `=` sign. The syntax `...` indicate that the argument "
				+ "can be repeated multiple times. For example::\n"
				+ "\n"
				+ "    ylim min max [track_regex = .*]...\n"
				+ "\n"
				+ "Means that `ylim` takes two mandatory arguments, `min` and `max`. The optional "
				+ "argument, `track_regex`, defaults to `.*` and can be repated multiple times.\n";
				
		String help= "";
		for(Section sec : Section.values()){
			
			help += toTitleCase(sec.toString() + "\n") + StringUtils.repeat("-", sec.toString().length()) + "\n\n";

			for(CommandHelp x : CommandList.getCommandsForSection(sec)){
								
				help += x.getPrintName() + "\n" + StringUtils.repeat("+", x.getPrintName().length()) + "\n\n"; 
				help += ":code:`" + (x.getName() + " " + x.getArgs()).trim() + "`\n\n";
				help += (x.getBriefDescription() + " " + x.getAdditionalDescription() + "\n\n");
			}			
		}
		// Handle special cases manually:
		help= help.replace("[incl_regex = .*]", "[incl_regex = .\\*]");
		
		help= help.replaceAll("~", " ");
		return intro + "\n\n" + help;
	}
	
	/** Run this method in Unit test to update the file commandHelp.md  
	 * @throws InvalidCommandLineException 
	 * @throws IOException 
	 * */
	public static void updateCommandHelpMdFile(File destFile) throws InvalidCommandLineException, IOException{
		
		BufferedWriter wr= new BufferedWriter(new FileWriter(destFile));
		wr.write(reStructuredTextHelp() + "\n");
		wr.close();
		System.err.println("Command help file written to " + destFile.getAbsolutePath());
	}
	
	public static String fullHelp() throws InvalidCommandLineException{
		String help= "\n      N a v i g a t i o n \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.NAVIGATION)){
			help += (x.printCommandHelp() + "\n");
		}
		help += "\n      F i n d \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.FIND)){
			help += (x.printCommandHelp() + "\n");
		}
		help += "\n      D i s p l a y \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.DISPLAY)){
			help += (x.printCommandHelp() + "\n");
		}
		
		help += "\n      A l i g n m e n t s \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.ALIGNMENTS)){
			help += (x.printCommandHelp() + "\n");
		}

		for(CommandHelp x : CommandList.getCommandsForSection(Section.GENERAL)){
			help += (x.printCommandHelp() + "\n");
		}
		
		help += "\n      G e n e r a l \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.GENERAL)){
			help += (x.printCommandHelp());
		}
		help += SEE_ALSO;
		return help;
	}
	
	public static String briefHelp() throws InvalidCommandLineException{
		String help= "\n      N a v i g a t i o n \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.NAVIGATION)){
			help += (x.printBriefHelp());
		}
		help += "\n      F i n d \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.FIND)){
			help += (x.printBriefHelp());
		}
		help += "\n      D i s p l a y \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.DISPLAY)){
			help += (x.printBriefHelp());
		}
		
		help += "\n      A l i g n m e n t s \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.ALIGNMENTS)){
			help += (x.printBriefHelp());
		}
		help += "\n      G e n e r a l \n\n";
		for(CommandHelp x : CommandList.getCommandsForSection(Section.GENERAL)){
			help += (x.printBriefHelp());
		}
		help += SEE_ALSO;
		return help;
	}

	
	private final static List<CommandHelp> commandHelpList() throws InvalidCommandLineException{
		List<CommandHelp> cmdList= new ArrayList<CommandHelp>();
		CommandHelp cmd= new CommandHelp();		

		cmd= new CommandHelp();
		cmd.setName("goto"); cmd.setArgs("chrom:[from]-[to]"); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription("Go to region `chrom:from-to` or to `chrom:from` or to the start of `chrom`. "); 
		cmd.setAdditionalDescription("The character ':' is a shortcut for `goto`. Examples::\n"
				+ "\n"
				+ "    goto chr8:1-1000~~## Go to interval 1-1000 on chr8\n"
				+ "    goto chr8:10~~~~~~## Go to position 10 on chr8\n"
				+ "    goto chr8~~~~~~~~~## Go to start of chr8\n"
				+ "\n"
				+ "Or the same with::\n"
				+ "\n"
				+ "    :chr8:1-1000 \n"
				+ "    :chr8:10 \n"
				+ "    :chr8"
				+ "\n");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("INT"); cmd.setArgs("[INT]"); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription(""
				+ "Go to position `INT` or to region `INT INT` on current chromosome. ");
		cmd.setAdditionalDescription(""
				+ "Allowed is the hyphenated format  separating the two positions. "
				+ "If a list of integers is given, the first and last are taken as *from* and *to*. "
				+ "This is handy to copy and paste intervals from the ruler above the prompt. "
				+ "\nExamples::\n"
				+ "\n"
				+ "    10~~~~~~~~~~~~~~~~~~~-> Will jump to position 10 \n"
				+ "    10 1000~~~~~~~~~~~~~~-> Go to region 10-1000 \n"
				+ "    10-1000~~~~~~~~~~~~~~-> Same as above\n"
				+ "    10 250 500 750 1000~~-> Same as above again\n"
				+ "\n");
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("+"); cmd.setArgs("INT [k|m]"); cmd.inSection= Section.NAVIGATION; cmd.setPrintName("plus +");
		cmd.setBriefDescription("Move forward by `INT` bases. Suffixes k (kilo) and M (mega) are expanded to x1000 and x1,000,000. "
				+ "Examples::\n"
				+ "\n"
				+ "    +2m\n"
				+ "    +10k\n"
				+ "    +10.5k\n"
				+ "\n"); 
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("-"); cmd.setArgs("INT [k|m]"); cmd.inSection= Section.NAVIGATION; cmd.setPrintName("minus -"); 
		cmd.setBriefDescription("Move backwards by INT bases. Suffixes k (kilo) and M (mega) are expanded to x1000 and x1,000,000.\n"
				+ "Examples::\n"
				+ "\n"
				+ "    -100\n"
				+ "    -10k\n"
				+ "    -10.5m\n"
				+ "\n"); 
		cmdList.add(cmd);
		
		cmd= new CommandHelp(); 
		cmd.setName("f"); cmd.setArgs("[NUM=0.1]"); cmd.inSection= Section.NAVIGATION; cmd.setPrintName("f - forward");
		cmd.setBriefDescription("Move forward NUM times the size of the current window, 1/10 by default.");  
		cmdList.add(cmd);
		
		cmd= new CommandHelp(); 
		cmd.setName("b"); cmd.setArgs("[NUM=0.1]"); cmd.inSection= Section.NAVIGATION; cmd.setPrintName("b - backward");
		cmd.setBriefDescription("Move backward NUM times the size of the current window, 1/10 by default"); 
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("ff"); cmd.setArgs(""); cmd.inSection= Section.NAVIGATION;
		cmd.setBriefDescription("Move forward by 1/2 of a window. A shortcut for `f 0.5`");  
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("bb"); cmd.setArgs(""); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription("Move backward by 1/2 of a window. A shortcut for `b 0.5`"); 
		cmdList.add(cmd);
						
		cmd= new CommandHelp();
		cmd.setName("zi"); cmd.setArgs("[INT = 1]"); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription("Zoom in INT times. Each zoom halves the window size. "); 
		cmd.setAdditionalDescription("To zoom quickly use INT=~5 or 10 e.g. `zi~10`");
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("zo"); cmd.setArgs("[INT = 1]"); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription("Zoom out INT times. Each zoom doubles the window size. ");
		cmd.setAdditionalDescription("To zoom quickly use INT=~5 or 10 e.g. `zo 10`");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("extend"); cmd.setArgs("[mid|window] [INT left] [INT right]"); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription("Extend the current window by `INT` bases left and right.\n");
		cmd.setAdditionalDescription("\n"
				+ "* :code:`window` (default): Extend the current window left and right by `INT` bases\n"
				+ "\n"
				+ "* :code:`mid`: The new window is given by the midpoint of the current window "
				+ "plus and minus `INT` bases left and right.\n"
				+ "\n"
				+ "If only one INT is given it is applied to both left and right. Negative INTs will shrink "
				+ "instead of extend the window.");
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("trim"); cmd.setArgs("track_name"); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription("Trim current coordinates to remove empty regions around `track_name`. ");
		cmd.setAdditionalDescription("With `track_name` missing trim on the first annotation track found. "
				+ "`track_name` can partially match the that actual, full track name; with multiple "
				+ "matches trim the first track found.");
		cmdList.add(cmd);
		
		
		cmd= new CommandHelp();
		cmd.setName("l"); cmd.setArgs(""); cmd.inSection= Section.NAVIGATION; cmd.setPrintName("l - left");
		cmd.setBriefDescription("Go to the Left half of the current window. ");
		cmd.setAdditionalDescription("Alternate the left and right command to quickly focus on a point of interest. ");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("r"); cmd.setArgs(""); cmd.inSection= Section.NAVIGATION; cmd.setPrintName("r - right");
		cmd.setBriefDescription("Go to the Right half of the current window. ");
		cmd.setAdditionalDescription("Alternate the left and right command to quickly focus on a point of interest. ");
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("p"); cmd.setArgs(""); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription("Go to the previous visited position. ");
		cmd.setAdditionalDescription("Similar to the back and forward arrows of an Internet browser.");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("n"); cmd.setArgs(""); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription("Go to the next visited position. ");
		cmd.setAdditionalDescription("Similar to the back and forward arrows of an Internet browser.");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("next"); cmd.setArgs("[-start] [track]"); cmd.inSection= Section.NAVIGATION; 
		cmd.setBriefDescription("Move to the next feature on `track` on current chromosome. "); 
		cmd.setAdditionalDescription(""
				+ "`next` centers the window on the found feature and zooms out. "
				+ "This is useful for quickly browsing through annotation files of genes or ChIP-Seq "
				+ "peaks in combination with read coverage tracks (bigwig, tdf, etc.).\n"
				+ "\n"
				+ "* :code:`-start`: Sets the window right at the start of the feature, without centering and zooming out.\n"
				+ "\n"
				+ "The `next` command does exactly that, it moves to the next feature. "
				+ "If there are no more features after the current position it doesn't rewind to the beginning "
				+ "(use `1` for that) and it doesn't move to another chromosome, "
				+ "use `goto chrom` for that.\n "
				+ "\n"
				+ "If `track` is omitted, the first annotation track is used."); 

		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("find"); cmd.setArgs("[-all] regex [track]"); cmd.inSection= Section.FIND; 
		cmd.setBriefDescription("Find the first record in `track` containing `regex`."); 
		cmd.setAdditionalDescription(""
				+ "The search for `regex` starts from the *end* of the current window "
				+ "(so the current window is not searched) and moves forward on the current chromosome. "
				+ "At the end  of the current chromosome move to the next chromosomes and then restart at "
				+ " the start of the initial one. The search stops at the first match found. If `track` is omitted "
				+ "the first interval track found is searched.\n"
				+ "The :code:`-all` flag will return the region containing **all** the regex matches.\n"
				+ "Examples::\n"
				+ "\n"
				+ "    find -all ACTB genes.gtf~-> Find all the matches of ACTB\n"
				+ "    find 'ACTB gene'~~~~~~~~~-> Find the first match of 'ACTB gene'\n"
				+ "\n"
				+ "Use single quotes to define patterns containing spaces."); 
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("seqRegex"); cmd.setArgs("[-iupac] [-c] [regex]"); cmd.inSection= Section.FIND; 
		cmd.setBriefDescription("Find regex in reference sequence and show matches as an additional track. ");
		cmd.setAdditionalDescription("Options:\n"
				+ "\n"
				+ "* :code:`regex`: Regex to search. If missing the seq regex track is removed.\n"
				+ "\n"
				+ "* :code:`-iupac`: Enable the interpretation of the IUPAC ambiguity code. NB: "
				+ "This option simply converts IUPAC chracters to the corresponding regex.\n"
				+ "\n"
				+ "* :code:`-c`: Enable case-sensitive matching. Default is to ignore case.\n"
				+ "\n"
				+ "Examples::\n"
				+ "\n"
				+ "    seqRegex~ACTG~~~~~~~~-> Case insensitive, actg matched\n"
				+ "    seqRegex -c ACTG ~~~~-> Case sensitive, will not match actg\n"
				+ "    seqRegex -iupac ARYG~-> Interpret (converts) R as [AG] and Y as [CT]\n"
				+ "    seqRegex~~~~~~~~~~~~~-> Disable regex matching track\n"
				+ "\n"
				+ "To save matches to file, see the `print` command. This command is ignored if the reference fasta sequence is missing."
				+ "");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("bookmark"); cmd.setArgs("[name] | [-rm] | [-print] | [> [file]]"); cmd.inSection= Section.FIND; 
		cmd.setBriefDescription("Creates a track to save positions of interest.");
		cmd.setAdditionalDescription(""
				+ "Without arguments, add the current position to the bookmark track. Options:\n"
				+ "\n"
				+ "* :code:`name`: give this name to the new bookmark.\n"
				+ "\n"
				+ "* :code:`-rm`: remove the bookmark matching *exactly* the current position.\n"
				+ "\n"
				+ "* :code:`-print`: prints to screen the list of current bookmarks.\n"
				+ "\n"
				+ "* :code:`>`: saves the bookmark track to file.\n"
				+ "\n"
				+ "Examples::\n"
				+ "\n"
				+ "    bookmark~~~~~~~~~~~~~~-> Add the current position to bookmarks.\n"
				+ "    bookmark myGene ~~~~~~-> Add the current position with name myGene\n"
				+ "    bookmark -rm ~~~~~~~~~-> Remove the bookmark exactly in this position\n"
				+ "    bookmark > books.txt~~-> Save to file books.txt\n"
				+ "    bookmark -print ~~~~~~-> Show table of bookmarks\n"
				+ "\n"
				+ "");
		cmdList.add(cmd);

		
		cmd= new CommandHelp();
		cmd.setName("grep"); cmd.setArgs("[-i = .*] [-e = ''] [track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Similar to grep command, filter for features including or excluding patterns.");
		cmd.setAdditionalDescription(""
				+ "Options:\n"
				+ "\n"
				+ "* :code:`-i regex`:  Show features matching this regex.\n"
				+ "\n"
				+ "* :code:`-e regex`: Exclude features matching this regex.\n"
				+ "\n"
				+ "* :code:`track_regex`: Apply to tracks matched by `track_regex`.\n"
				+ "\n"
				+ "Regex `-i` and `-e` are applied to the raw lines as read from source file. "
				+ "This command is useful to filter the annotation in GTF or BED files, for example::\n"
				+ "\n"
				+ "    grep -i RNA -e mRNA gtf gff\n"
				+ "\n"
				+ "Will show the rows containing 'RNA' but will hide those containing 'mRNA', applies "
				+ "to tracks whose name matches 'gtf' or 'gff'."
				+ ""
				+ "\nWith no arguments reset to default: :code:`grep -i .* -e ^$ .*` which means show everything, hide nothing, apply to all tracks."
				);
		cmdList.add(cmd);		

		cmd= new CommandHelp();
		cmd.setName("squash"); cmd.setArgs("[track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Toggle the squashing of features with the same coordinates. ");
		cmd.setAdditionalDescription("If set, features with the same start, end, and strand are squashed in a single one. "
				+ "The displayed feature is the first one found in the group of features with the same coordinates. "
				+ "Useful to compact GTF where e.g. CDS and exons have the same coordinates. "
				+ "Applies only to annotation tracks captured by track_regex");
		cmdList.add(cmd);		

		cmd= new CommandHelp();
		cmd.setName("merge"); cmd.setArgs("[track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Toggle the merging of overlapping features.");
		cmd.setAdditionalDescription("If set, features with overalapping coordinates are merged in a single one. "
				+ "Merged features will not have strand and name information. Note that merging is done without considering strand information. "
				+ "Applies only to annotation tracks captured by the list of track_regex");
		cmdList.add(cmd);		

		cmd= new CommandHelp();
		cmd.setName("gap"); cmd.setArgs("[track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Toggle the switch to add a gap between features. Default is true.");
		cmd.setAdditionalDescription("If gap is set, as per default, features which on screen do not have "
				+ "at least one space separating are moved to different lines so that it is clear where is the end of one "
				+ "feature  and the start of the next one. "
				+ "If gap is unset such features might appear as a single, continuous one instaed. "
				+ "\n"
				+ "Example with gap set::\n"
				+ "\n"
				+ "    ||||||\n"
				+ "    ~~~~~~||||||\n"
				+ "\n"
				+ "With gap unset these two features look like::\n"
				+ "\n"
				+ "    ||||||||||||\n"
				+ "\n"
				+ "Gap unset is preferable when the interest is in knowing which regions are covered "
				+ "since it gives a more compact view "
				+ "and the distiction betwen adjacent features is not important.");
		cmdList.add(cmd);		

		
		cmd= new CommandHelp();
		cmd.setName("gffNameAttr"); cmd.setArgs("[attribute_name = NULL | -na] [track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("GTF/GFF attribute to set the feature name or `-na` to suppress name. ");
		cmd.setAdditionalDescription("Use attribute NULL to reset to default choice of attribute. To suppress "
				+ "printing of the name use `-na`. Bed features get their name from the 4th column. "
				+ "Applies to annotation tracks captured by the list `track_regex`. Example, given "
				+ "the gtf feature::\n"
				+ "\n"
				+ "    chr1 . CDS  10 99 . + 2 gene_id \"PTGFRN\"; transcript_id \"NM_020440\";\n"
				+ "\n"
				+ "Use gene_name as feature name or transcript_id::\n"
				+ "\n"
				+ "    gffNameAttr gene_name genes.gtf .*gff\n"
				+ "    PTGFRN_CCCCCCCCC\n"
				+ "    \n"
				+ "    gffNameAttr transcript_id genes.gtf .*gff\n"
				+ "    NM_020440_CCCCCC\n"
				+ "    \n"
				+ "    gffNameAttr -na\n"
				+ "    CCCCCCCCCCCCCCCC <- Do not show name"
				+ "    \n"
				+ "\n");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("trackHeight"); cmd.setArgs("INT [track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Set track height to INT lines of text for all tracks matching regexes. ");
		cmd.setAdditionalDescription("Setting height to zero hides the track and skips the processing altogether. "
				+ "This is useful to speed up the browsing when large bam files are present. Use infoTrack "
				+ "to see which tracks are hidden. Example::\n"
				+ "\n"
				+ "    trackHeight 5 aln.*bam gtf`"
				+ "\n");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("ylim"); cmd.setArgs("<NUM|min|na> <NUM|min|na> [track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Set the y-axis limit for all tracks matched by regexes.");
		cmd.setAdditionalDescription("The first two arguments set the min and max limits. The 3rd "
				+ "argument is a list of regexes to capture the tracks to reset. "
				+ "Argument min and max can be:\n"
				+ "\n"
				+ "* :code:`NUM`: Numeric, fix the limits exactly to these values\n"
				+ "\n"
				+ "* :code:`na`: Scale tracks to their individual min and/or max\n"
				+ "\n"
				+ "* :code:`min` and :code:`max`: Set to the min and max of **all** tracks.\n"
				+ "\n"
				+ "This command applies only to tracks displaying quantitative data on y-axis (e.g. bigwig, tdf), "
				+ "the other tracks are unaffected. Examples::\n"
				+ "\n"
				+ "    ylim 0 50~~~~~~-> Set min= 0 and max= 50 in all tracks.\n"
				+ "    ylim 0 na~~~~~~-> Set min to 0 and autoscale the max. Apply to all tracks\n"
				+ "    ylim na na tdf~-> Autoscale min and max. Apply to all tracks matching 'tdf'\n"
				+ "    ylim min max~~~-> Set to the min and max of all tracks\n"
				+ "\n");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("colorTrack"); cmd.setArgs("color [track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Set colour for tracks matched by regex. ");
		cmd.setAdditionalDescription(""
				+ "Available colours: red, green, yellow, blue, magenta, cyan, grey, "
				+ "light_red, light_green, light_yellow, light_blue, light_magenta, light_cyan, light_grey, "
				+ "white, black, default. The 'default' colour reset to the system default colour. "
				+ "Colouring is rendered with ANSI codes 8/16. Example::\n"
				+ "\n"
				+ "    colorTrack~light_blue~ts.*gtf~ts.*bam\n"
				+ "\n");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("hideTitle"); cmd.setArgs("[track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Toggle the display of the title line matched by track_regex.");
		cmd.setAdditionalDescription("Use /hide_all/ and /show_all/ to hide all tracks or show all tracks instead "
				+ "of toggling their mode. Examples::\n"
				+ "\n"
				+ "    hideTitle~~~~~~~~~~~~-> Toggle all tracks, same as hideTitle .*\n"
				+ "    hideTitle~bam~bed~~~~-> Toggle all tracks matched by 'bam' or 'bed'\n"
				+ "    hideTitle~/hide_all/~-> Hide all tracks\n"
				+ "");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("editNames"); cmd.setArgs("-t <pattern> <replacement> [track_re=.*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Edit track names by substituting regex pattern with replacement.");
		cmd.setAdditionalDescription("Pattern and replacement are required arguments, "
				+ "the default regex for track is '.*' (i.e. all tracks).\n"
				+ "The :code:`-t` (test) flag shows what renaming would be done without actually editing the names.\n"
				+ "Use \"\" (empty double quotes) to replace pattern with nothing. "
				+ "Examples: Given track names 'fk123_hela.bam#1' and 'fk123_hela.bed#2'::\n"
				+ "\n"
				+ "    editNames fk123_ \"\"~~~~-~> hela.bam#1, hela.bed#2\n"
				+ "    editNames fk123_ \"\" bam -> hela.bam#1, fk123_hela.bed#2\n"
				+ "    editNames _ ' ' ~~~~~~~~~~-> fk123 hela.bam#1,  fk123 hela.bed#2\n"
				+ "    editNames ^.*# cells ~~~~~-> cells#1, cells#2\n"
				+ "    editNames ^ xx_ ~~~~~~~~~~-> xx_fk123_hela.bam#1, xx_fk123_hela.bed#2 (add prefix)\n"
				+ "");
		cmdList.add(cmd);

		
		
		cmd= new CommandHelp();
		cmd.setName("dataCol"); cmd.setArgs("[index = 4] [track_regex = .*]..."); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Select data column for bedgraph tracks containing regex. ");
		cmd.setAdditionalDescription("index: 1-based column index. This command applies only to "
				+ "tracks of type bedgraph.\n For example, use column 5 on tracks containing #1 and #3::\n "
				+ "\n"
				+ "    dataCol 5 #1 #3\n"
				+ "\n");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("print"); cmd.setArgs("[-full] [-off] [track_regex = .*]... [>|>> file]"); cmd.inSection= Section.DISPLAY; 
		cmd.setBriefDescription("Toggle the printing of lines for the tracks matched by `track_regex`. ");
		cmd.setAdditionalDescription("Useful to show exactly what features are present in the current window. "
				+ "Features are filtered in/out according to the :code:`grep` command. Options:\n"
				+ "\n"
				+ "* :code:`track_regex`: Toggle printing of the tracks matched by one or more of these regexes.\n"
				+ "\n"
				+ "* :code:`-full`: Wrap lines longer than the screen width. Default is to clip them.\n"
				+ "\n"
				+ "* :code:`-off`: Turn off printing for *all* tracks, regardless of their current mode. The list of regexes is effectively ignored and set to '.*'.\n"
				+ "\n"
				+ "* :code:`>` and :code:`>>`: Send the output to `file` instead of to screen. `>` overwrites existing file, `>>` appends. "
				+ "Redirecting to file is probably not useful if more than track is selected as files will overwrite each other or be appended. "
				+ "The %r variable is expanded to the current genomic coordinates.\n"
				+ "\n"
				+ "Examples::\n"
				+ "    print~~~~~~~~~~~~~~~~~~~~~~~~-> Print all tracks, same as `print .*`\n"
				+ "    print -off~~~~~~~~~~~~~~~~~~~-> Turn off printing for all tracks\n"
				+ "    print genes.bed >> genes.txt~-> Append features in track(s) 'genes.bed' to file\n"
				+ "\n"
				+ "Currently `print` applies only to annotation tracks, other tracks are unaffected.");
		cmdList.add(cmd);

//		cmd= new CommandHelp();
//		cmd.setName("gcProfile"); cmd.setArgs(""); cmd.inSection= Section.DISPLAY; 
//		cmd.setBriefDescription("Toggle display of GC content profile.");
//		cmd.setAdditionalDescription("The GC content profile is shown if the reference fasta sequence is available.");
//		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("setGenome"); cmd.setArgs("fasta|bam|genome"); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Set genome and reference sequence.");
		cmd.setAdditionalDescription("The genome, i.e. the list of contig and names and sizes, "
				+ "can be extracted from the indexed fasta reference, from a bam file or from "
				+ "a genome identifier (e.g. hg19). If a fasta file is used also the "
				+ "reference sequence becomes available.");
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("showGenome"); cmd.setArgs(""); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Print the genome dictionary with a representation of chromosome sizes. ");
		cmd.setAdditionalDescription("Example output::\n"
				+ "\n"
				+ "    showGenome\n"
				+ "    chrM  16571\n"
				+ "	   chr1  249250621 ||||||||||||||||||||||||||||||\n"
				+ "    chr2  243199373 |||||||||||||||||||||||||||||\n"
				+ "    ...\n"
				+ "    chr21 48129895  ||||||\n"
				+ "    chr22 51304566  ||||||\n"
				+ "    chrX  155270560 |||||||||||||||||||\n"
				+ "    chrY  59373566  |||||||\n"
				+ "\n"
				+ "");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("infoTracks"); cmd.setArgs(""); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Print the name of the current tracks along with file name and format. ");
		cmd.setAdditionalDescription("Hidden tracks are marked by an asterisk.");
		cmdList.add(cmd);

		
		cmd= new CommandHelp();
		cmd.setName("addTracks"); cmd.setArgs("[file or URL]..."); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Add tracks from local or remote files.");
		cmd.setAdditionalDescription("\n"
				+ "Examples::\n"
				+ "\n"
				+ "    addTracks peaks.bed gene.gtf\n"
				+ "    addTracks http://remote/host/peaks.bed\n"
				+ "");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("dropTracks"); cmd.setArgs("[-t] track_regex [track_regex]..."); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Drop tracks matching any of the listed regexes.");
		cmd.setAdditionalDescription("The :code:`-t` (test) flag only shows what tarcks would be removed without "
				+ "actually removing them.\n"
				+ "Examples::\n"
				+ "\n"
				+ "    dropTracks bam\n"
				+ "");
		cmdList.add(cmd);

		
		cmd= new CommandHelp();
		cmd.setName("orderTracks"); cmd.setArgs("[track_regex]..."); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Reorder tracks according to the list of regexes or sort by name.");
		cmd.setAdditionalDescription("Not all the tracks need to be listed, the missing ones "
				+ "follow the listed ones in unchanged order. Without arguments sort track by tag name.\n"
				+ "For example, given the track list: `[hela.bam#1, hela.bed#2, hek.bam#3, hek.bed#4]`::\n"
				+ "\n"
				+ "    orderTracks #2 #1~~~->~[hela.bed#2, hela.bam#1, hek.bam#3, hek.bed#4]\n"
				+ "    orderTracks bam bed~->~[hela.bam#1, hek.bam#3, hela.bed#2, hek.bed#4]\n"
				+ "    orderTracks~~~~~~~~~->~name sort~[hela.bam#1, hela.bed#2, hek.bam#3, hek.bed#4]\n"
				+ "");
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("history"); cmd.setArgs(""); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Show the list of visited positions.");
		cmd.setAdditionalDescription("");
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("cmdHistory"); cmd.setArgs(""); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Show the list of executed commands.");
		cmd.setAdditionalDescription("");
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("rpm"); cmd.setArgs("[track_regex = .*]"); cmd.inSection= Section.ALIGNMENTS; 
		cmd.setBriefDescription("Toggle read coverage from raw count to reads per million. Applys to BAM and TDF files.");
		cmd.setAdditionalDescription("");
		cmdList.add(cmd);		

		cmd= new CommandHelp();
		cmd.setName("samtools"); cmd.setArgs("[-f INT=0] [-F INT=4] [-q INT=0] [track_re = .*] ..."); cmd.inSection= Section.ALIGNMENTS; 
		cmd.setBriefDescription("Apply samtools filters to alignment tracks captured by the list of track regexes.");
		cmd.setAdditionalDescription("As *samtools view*, this command filters alignment records on the basis "
				+ "of the given flags:\n"
				+ "\n"
				+ "* :code:`-F`: Filter out flags with these bits set. NB: 4 is always set.\n"
				+ "\n"
				+ "* :code:`-f`: Require alignment to have these bits sets.\n"
				+ "\n"
				+ "* :code:`-q`: Require alignments to have MAPQ >= than this.\n"
				+ "\n"
				+ "Examples::\n"
				+ "\n"
				+ "    samtools -q 10~~~~~~~~~~~-> Set mapq for all tracks. -f and -F reset to default\n"
				+ "    samtools -F 1024 foo bar -> Set -F for all track containing re foo or bar\n"
				+ "    samtools~~~~~~~~~~~~~~~~~-> Reset all to default.\n"
				+ "");
		cmdList.add(cmd);		

		cmd= new CommandHelp();
		cmd.setName("BSseq"); cmd.setArgs("[track_regex = .*]..."); cmd.inSection= Section.ALIGNMENTS; 
		cmd.setBriefDescription("Toggle bisulfite mode for read tracks matched by regex.");
		cmd.setAdditionalDescription("In bisulfite mode, the characters M and m mark methylated bases "
				+ "(i.e. unconverted C to T) and U and u are used for unmethylated bases "
				+ "(i.e. C converted to T). Upper case is used for reads on  forward strand, small case for reverse. "
				+ "Ignored without reference fasta sequence.");
		cmdList.add(cmd);		

		//cmd= new CommandHelp();
		//cmd.setName("pileup"); cmd.setArgs("[track_regex = .*]..."); cmd.inSection= Section.ALIGNMENTS; 
		//cmd.setBriefDescription("Print pileup of nucleotide counts.");
		//cmd.setAdditionalDescription("...");
		//cmdList.add(cmd);		
		
		cmd= new CommandHelp();
		cmd.setName("save"); cmd.setArgs("[filename = chrom_start_end.txt']"); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Save current screenshot to file in either text or pdf format.");
		cmd.setAdditionalDescription("Default filename is generated from the current coordinates and the default format is plain text. "
				+ "If filename has extension pdf then save as pdf. "
				+ "The string `%r` in the file name is replaced with the current coordinates. "
				+ "Examples::\n"
				+ "\n"
				+ "    save mygene.txt~~~~-> Save to mygene.txt as text\n"
				+ "    save~~~~~~~~~~~~~~~-> Save to chrom_start-end.txt as text\n"
				+ "    save .pdf~~~~~~~~~~-> Save to chrom_start-end.png as pdf\n"
				+ "    save mygene.%r.pdf~-> Save to mygene.chr1_100-200.png as pdf\n"
				+ "\n");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("sessionSave"); cmd.setArgs("filename"); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Experimental: Save the current settings to file suitable to be reloaded by ASCIIGenome.");
		cmd.setAdditionalDescription("`sessionSave` writes to file a set of commands to reproduce the current "
				+ "settings: tracks, colors, heights etc. It's not meant to be a perfect replica, rather it's a "
				+ "shortcut to avoid re-typing commands. Example::\n"
				+ "\n"
				+ "    sessionSave session.txt\n"
				+ "\n"
				+ "Quit session and reload with::\n"
				+ "\n"
				+ "    ASCIIGenome -x session.txt\n"
				+ "");
		cmdList.add(cmd);
		
		cmd= new CommandHelp();
		cmd.setName("q"); cmd.setArgs(""); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("Quit");
		cmd.setAdditionalDescription("");
		cmdList.add(cmd);

		cmd= new CommandHelp();
		cmd.setName("h"); cmd.setArgs("-h"); cmd.inSection= Section.GENERAL; 
		cmd.setBriefDescription("h and -h show this help.\n"
				+ "For help on commands: `command -h`, e.g. :code:`ylim -h`");
		cmd.setAdditionalDescription("");
		cmdList.add(cmd);
				
		// Make sure ther are no undocumented cmds
		List<String> documented= new ArrayList<String>();
		for(CommandHelp x : cmdList){
			if(documented.contains(x.getName())){
				System.err.println(x.getName() + " already documented!");
				throw new InvalidCommandLineException();
			}
			documented.add(x.getName());
		}
		for(String x : CommandList.cmds()){
			if(!documented.contains(x)){
				System.err.println("Undocumented command: " + x);
				// throw new InvalidCommandLineException();
			}
		}
		
		return cmdList;
			
		}

	protected static List<CommandHelp> getCommandsForSection(Section section) throws InvalidCommandLineException{
		List<CommandHelp> cmdList= new ArrayList<CommandHelp>();
		for(CommandHelp x : commandHelpList()){
			if(x.inSection.equals(section)){
				cmdList.add(x);
			}
		}
		return cmdList;
	}
	
	
	/* Known commnds */
	protected static final List<String> cmds(){
		List<String> paramList= new ArrayList<String>();
		paramList.add("q");
		paramList.add("h");
		paramList.add("f");
		paramList.add("b");
		paramList.add("ff");
		paramList.add("bb");
		paramList.add("zi");
		paramList.add("zo");
		paramList.add("extend");
		paramList.add("trim");
		paramList.add("l");
		paramList.add("r");
		paramList.add("goto");
		paramList.add("INT");
		paramList.add("+");
		paramList.add("-");
		paramList.add("p");
		paramList.add("n");
		paramList.add("next");
		paramList.add("find");
		paramList.add("seqRegex");
		paramList.add("bookmark");
		// paramList.add("gcProfile");
		paramList.add("grep");
		paramList.add("gffNameAttr");
		paramList.add("squash");
		paramList.add("merge");
		paramList.add("gap");
		paramList.add("trackHeight");
		paramList.add("colorTrack");
		paramList.add("hideTitle");
		paramList.add("editNames");
		paramList.add("ylim");
		paramList.add("dataCol");
		paramList.add("print");
		paramList.add("setGenome");
		paramList.add("showGenome");
		paramList.add("infoTracks");
		paramList.add("addTracks");
		paramList.add("dropTracks");
		paramList.add("orderTracks");
		paramList.add("history");
		paramList.add("cmdHistory");
		paramList.add("rpm");
		// paramList.add("pileup");
		paramList.add("samtools");
		paramList.add("BSseq");
		paramList.add("save");
		paramList.add("sessionSave");
	
		return paramList;
	}

	public static String getHelpForCommand(String commandName) {
		try {
			for(CommandHelp x : CommandList.commandHelpList()){
				if(x.getName().equals(commandName)){
					return x.printCommandHelp();
				}
			}
		} catch (InvalidCommandLineException e1) {
			e1.printStackTrace();
		}
		return "";
	}

	private static String toTitleCase(String x){
		x= x.toLowerCase();
		return x.substring(0, 1).toUpperCase() + x.substring(1);
	}
}
