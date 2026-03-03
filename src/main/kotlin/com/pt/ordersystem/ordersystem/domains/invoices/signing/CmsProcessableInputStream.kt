package com.pt.ordersystem.ordersystem.domains.invoices.signing

import org.apache.pdfbox.io.IOUtils
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers
import org.bouncycastle.cms.CMSTypedData
import java.io.InputStream
import java.io.OutputStream

internal class CmsProcessableInputStream(
  private val inputStream: InputStream,
  private val contentType: ASN1ObjectIdentifier = ASN1ObjectIdentifier(CMSObjectIdentifiers.data.id)
) : CMSTypedData {

  override fun getContent(): Any = inputStream

  override fun write(out: OutputStream) {
    IOUtils.copy(inputStream, out)
    inputStream.close()
  }

  override fun getContentType(): ASN1ObjectIdentifier = contentType
}
