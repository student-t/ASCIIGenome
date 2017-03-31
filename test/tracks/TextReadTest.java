package tracks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import coloring.Config;
import exceptions.InvalidColourException;
import exceptions.InvalidConfigException;
import exceptions.InvalidGenomicCoordsException;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import samTextViewer.GenomicCoords;

public class TextReadTest {

	static SamReaderFactory srf=SamReaderFactory.make();
	static SamReader samReader= srf.open(new File("test_data/ds051.short.bam"));
	public static SAMSequenceDictionary samSeqDict= samReader.getFileHeader().getSequenceDictionary();
	
	public static String fastaFile= "test_data/chr7.fa";
	
	@Test
	public void getNoAlignedBasesFromCigar() {
		// A reminder of how cigar works.
		SAMRecord rec= new SAMRecord(null);
		rec.setCigarString("2M2D4M");
		int noRefBases= rec.getCigar().getReferenceLength();
		assertEquals(8, noRefBases);
		
		rec.setCigarString("2M2N4M"); // N same as D
		noRefBases= rec.getCigar().getReferenceLength();
		assertEquals(8, noRefBases);
		
		rec.setCigarString("2M2I4M");
		noRefBases= rec.getCigar().getReferenceLength();
		assertEquals(6, noRefBases);
		
		rec.setCigarString("2S10M4H");
		noRefBases= rec.getCigar().getReferenceLength();
		assertEquals(10, noRefBases);		
		
		// Cigar oprators consuming:
		rec.setCigarString("2H"); // Hard clipping doesn't consume bases on read or ref
		CigarElement el= rec.getCigar().getCigarElement(0);
		assertFalse(el.getOperator().consumesReadBases());		
		assertFalse(el.getOperator().consumesReferenceBases());

		rec.setCigarString("2S"); // Clips the read not the ref.
		el= rec.getCigar().getCigarElement(0);
		assertTrue(el.getOperator().consumesReadBases());		
		assertFalse(el.getOperator().consumesReferenceBases());

		rec.setCigarString("2D"); // This makes a gap in the read when aligned to ref
		el= rec.getCigar().getCigarElement(0);
		assertFalse(el.getOperator().consumesReadBases());		
		assertTrue(el.getOperator().consumesReferenceBases());

		rec.setCigarString("2I"); // Opposite of D
		el= rec.getCigar().getCigarElement(0);
		assertTrue(el.getOperator().consumesReadBases());		
		assertFalse(el.getOperator().consumesReferenceBases());
	}

	@Test
	public void canPrintDNARead() throws InvalidGenomicCoordsException, IOException, InvalidColourException, InvalidConfigException{
		
		new Config(null);
		
		GenomicCoords gc= new GenomicCoords("chr7:5566778-5566798", samSeqDict, fastaFile);
		SAMRecord rec= new SAMRecord(null);
		rec.setAlignmentStart(5566780);
		rec.setCigarString("24M");
		rec.setReadNegativeStrandFlag(true);
		rec.setReadBases("AACCGGTTAACCGGTTAACCGGTT".getBytes());
		
		TextRead textRead= new TextRead(rec, gc);
		assertEquals(textRead.getTextStart(), 3);
		assertEquals(textRead.getTextEnd(), 21);
		assertEquals("AACCGGTTAACCGGTTAAC".length(), textRead.getTextEnd() - textRead.getTextStart() + 1);
		
		System.out.println(textRead.getPrintableTextRead(false, true, false, gc.getBpPerScreenColumn()));
		
		assertEquals("a,ccgg,t,acc,gtt,ac", textRead.getPrintableTextRead(false, true, false, gc.getBpPerScreenColumn()));
		assertEquals("a,ccgg,t,uccmgtt,ac", textRead.getPrintableTextRead(true, true, false, gc.getBpPerScreenColumn()));


		gc= new GenomicCoords("chr7:5566778-5566798", samSeqDict, null);
		textRead= new TextRead(rec, gc);
		assertEquals("aaccggttaaccggttaac", textRead.getPrintableTextRead(false, true, false, gc.getBpPerScreenColumn()));

		gc= new GenomicCoords("chr7:5566780-5566782", samSeqDict, fastaFile);
		textRead= new TextRead(rec, gc);
		System.out.println(textRead.getPrintableTextRead(true, true, false, gc.getBpPerScreenColumn()));
		System.out.println(textRead.getPrintableTextRead(true, false, false, gc.getBpPerScreenColumn()));		
	}

	@Test
	public void canPrintSquashedRead() throws InvalidGenomicCoordsException, IOException, InvalidColourException{
		GenomicCoords gc= new GenomicCoords("chr7:5566778-5566978", samSeqDict, fastaFile);
		
		SAMRecord rec= new SAMRecord(null);
		rec.setAlignmentStart(5566780);
		rec.setCigarString("24M");
		rec.setReadBases("AACCGGTTAACCGGTTAACCGGTT".getBytes());
		
		TextRead textRead= new TextRead(rec, gc);
		assertEquals(2, textRead.getTextStart());
		assertEquals(11, textRead.getTextEnd());
		assertEquals(textRead.getTextEnd() - textRead.getTextStart() + 1, ">>>>>>>>>>".length());
		assertEquals(textRead.getPrintableTextRead(false, true, false, gc.getBpPerScreenColumn()), ">>>>>>>>>>");
		// System.out.println(textRead);
	}
	
	@Test
	public void canPrintWithReadName() throws InvalidGenomicCoordsException, IOException, InvalidColourException{

		GenomicCoords gc= new GenomicCoords("chr7:5566778-5566978", samSeqDict, fastaFile);
		
		SAMRecord rec= new SAMRecord(null);
		rec.setAlignmentStart(5566780);
		rec.setCigarString("24M");
		rec.setReadBases("AACCGGTTAACCGGTTAACCGGTT".getBytes());
		rec.setReadName("Read1");
		
		TextRead textRead= new TextRead(rec, gc);
		assertEquals("Read1/>>>>", textRead.getPrintableTextRead(false, true, true, gc.getBpPerScreenColumn()));
		
		rec.setReadName("VeryLongReadNameMoreThanReadSequence");
		textRead= new TextRead(rec, gc);
		assertEquals("VeryLongRe", textRead.getPrintableTextRead(false, true, true, gc.getBpPerScreenColumn()));
		
		rec.setReadName("VeryLongRead");
		textRead= new TextRead(rec, gc);
		assertEquals("VeryLongRe", textRead.getPrintableTextRead(false, true, true, gc.getBpPerScreenColumn()));
	}
}
