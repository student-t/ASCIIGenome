Test files not in repository
============================

Some files are not in the repository as they are publicly available and quite
large. These are the commands to get these files, they must be stored in `test_data` directory
for the tests to find them:

Downloads
---------

```
cd /path/to/test_data

curl -C - -O http://hgdownload.cse.ucsc.edu/goldenpath/hg19/chromosomes/chr7.fa.gz 
gunzip chr7.fa.gz
## md5sum chr7.fa  ## if linux
md5 chr7.fa        ## if MacOS ## 30c3693ead968844a769a90a801a900f
samtools faidx chr7.fa

curl -C - -O http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/refGene.txt.gz
zcat refGene.txt.gz | grep '\tchr7\t' | gzip > refGene.hg19.chr7.txt.gz
rm refGene.txt.gz

curl -C - -O http://ftp.ebi.ac.uk/pub/databases/ensembl/encode/integration_data_jan2011/byDataType/openchrom/jan2011/fdrPeaks/wgEncodeDukeDnase8988T.fdr01peaks.hg19.bb

curl -C - -O ftp://ftp.ensembl.org/pub/release-86/gff3/homo_sapiens/Homo_sapiens.GRCh38.86.chromosome.7.gff3.gz

curl -C - -O http://hgdownload.cse.ucsc.edu/goldenpath/hg19/encodeDCC/wgEncodeCaltechRnaSeq/wgEncodeCaltechRnaSeqGm12878R2x75Il400SigRep2V2.bigWig

curl -C - -O ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/phase1/analysis_results/input_call_sets/ALL.wex.union_illumina_wcmc_bcm_bc_bi.20110521.snps.exome.sites.vcf.gz &&
gunzip ALL.wex.union_illumina_wcmc_bcm_bc_bi.20110521.snps.exome.sites.vcf.gz
```

Derived data
------------

```
## This gzip is in repo but unzipped is not:
gunzip -c CEU.exon.2010_06.genotypes.vcf.gz > CEU.exon.2010_06.genotypes.vcf
gunzip -c ALL.wgs.mergedSV.v8.20130502.svs.genotypes.vcf.gz > ALL.wgs.mergedSV.v8.20130502.svs.genotypes.vcf

## Pileup counting *everything*
samtools mpileup -A -q 0 -Q 0 -x --ff 0 ear045.oxBS.actb.bam > ear045.oxBS.actb.pileup
```

**Memo**:

If you add more of these files, remember to list them also in `svn_ignore.txt`
and reload the `svn_ignore.txt` files:
 
```
cd /path/to/test_data
svn propset svn:ignore -F svn_ignore.txt .
``` 
