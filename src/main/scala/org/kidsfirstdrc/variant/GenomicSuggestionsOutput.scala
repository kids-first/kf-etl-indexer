package org.kidsfirstdrc.variant

case class GenomicSuggestionsOutput(`type`: String = "variant",
                                    `locus`: String = "2-165310406-G-A",
                                    `suggestion_id`: String = "ba3d35feba14451058e6fc93eeba163c800a8e09",
                                    `hgvsg`: String = "chr2:g.166166916G>A",
                                    `symbol`: String = "SCN2A",
                                    `suggest`: List[SUGGEST] = List(SUGGEST(), SUGGEST(List("SCN2A", "SCN2A.2"), 2)))


case class SUGGEST(`input`: List[String] = List("SCN2A V261E", "SCN2A.2 V261M", "chr2:g.166166916G>A", "2-165310406-G-A"),
                   `weight`: Long = 4)
