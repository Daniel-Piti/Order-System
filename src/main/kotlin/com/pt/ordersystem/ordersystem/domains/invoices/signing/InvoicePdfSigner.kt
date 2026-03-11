package com.pt.ordersystem.ordersystem.domains.invoices.signing

import com.pt.ordersystem.ordersystem.config.ApplicationConfig
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.COSDictionary
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.common.PDStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField
import org.apache.pdfbox.pdmodel.PDResources
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import jakarta.annotation.PostConstruct
import java.security.PrivateKey
import java.util.Base64

@Component
class InvoicePdfSigner(
  config: ApplicationConfig,
) : SignatureInterface {

  private val signingConfig = config.invoiceSigning
  private lateinit var privateKey: PrivateKey
  private lateinit var certificateChain: Array<Certificate>

  companion object {
    private val logger = LoggerFactory.getLogger(InvoicePdfSigner::class.java)
    private const val SIGNATURE_FONT_PATH = "fonts/DejaVuSans.ttf"
    private const val VISIBLE_SIG_WIDTH = 200f
    private const val VISIBLE_SIG_HEIGHT = 28f
    private const val VISIBLE_SIG_MARGIN_BOTTOM = 20f
    private const val VISIBLE_SIG_MARGIN_SIDE = 40f
  }

  @PostConstruct
  fun init() {
    require(signingConfig.keystoreBase64.isNotBlank()) {
      "Invoice signing is required: INVOICE_SIGNING_KEYSTORE_BASE64 must be set"
    }
    require(signingConfig.keystorePassword.isNotBlank()) {
      "Invoice signing is required: INVOICE_SIGNING_KEYSTORE_PASSWORD must be set"
    }
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(BouncyCastleProvider())
    }
    loadKeystore()
  }

  private fun loadKeystore() {
    val keyStore = KeyStore.getInstance("PKCS12")
    val bytes = Base64.getDecoder().decode(signingConfig.keystoreBase64)
    ByteArrayInputStream(bytes).use { fis ->
      keyStore.load(fis, signingConfig.keystorePassword.toCharArray())
    }
    val alias = signingConfig.keyAlias.ifBlank {
      keyStore.aliases().toList().firstOrNull()
        ?: throw IllegalStateException("Keystore has no aliases")
    }
    val keyPassword = if (signingConfig.keyPassword.isNotBlank()) signingConfig.keyPassword.toCharArray()
    else signingConfig.keystorePassword.toCharArray()
    privateKey = keyStore.getKey(alias, keyPassword) as PrivateKey
    certificateChain = keyStore.getCertificateChain(alias)
      ?: throw IllegalStateException("No certificate chain for alias: $alias")
    (certificateChain[0] as X509Certificate).checkValidity()
    logger.info("Invoice PDF signer initialized with alias=$alias")
  }

  override fun sign(content: InputStream): ByteArray {
    val gen = CMSSignedDataGenerator()
    val cert = certificateChain[0] as X509Certificate
    val signer = JcaContentSignerBuilder("SHA256WithRSA").build(privateKey)
    gen.addSignerInfoGenerator(
      JcaSignerInfoGeneratorBuilder(JcaDigestCalculatorProviderBuilder().build()).build(signer, cert)
    )
    gen.addCertificates(JcaCertStore(certificateChain.toList()))
    val msg = CmsProcessableInputStream(content)
    val signedData = gen.generate(msg, false)
    return signedData.encoded
  }

  /**
   * Signs the given PDF bytes and returns signed PDF bytes.
   * Adds a visible signature (Hebrew text) on the last page.
   */
  fun sign(unsignedPdf: ByteArray, signerDisplayName: String): ByteArray {
    Loader.loadPDF(unsignedPdf).use { doc ->
      val accessPermissions = getMDPPermission(doc)
      if (accessPermissions == 1) {
        throw IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary")
      }
      val lastPageIndex = doc.numberOfPages - 1
      val rect = PDRectangle(
        VISIBLE_SIG_MARGIN_SIDE,
        VISIBLE_SIG_MARGIN_BOTTOM,
        VISIBLE_SIG_WIDTH,
        VISIBLE_SIG_HEIGHT
      )
      val signature = PDSignature()
      signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE)
      signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED)
      signature.name = signerDisplayName
      signature.location = "Israel"
      signature.reason = "חשבונית מס־קבלה ממוחשבת"
      signature.signDate = Calendar.getInstance(TimeZone.getDefault(), Locale.forLanguageTag("he-IL"))
      val signatureOptions = SignatureOptions()
      signatureOptions.preferredSignatureSize = SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2
      signatureOptions.setVisualSignature(createVisualSignatureTemplate(doc, lastPageIndex, rect))
      signatureOptions.page = lastPageIndex
      doc.addSignature(signature, this, signatureOptions)
      val output = ByteArrayOutputStream()
      doc.saveIncremental(output)
      IOUtils.closeQuietly(signatureOptions)
      return output.toByteArray()
    }
  }

  private fun getMDPPermission(doc: PDDocument): Int {
    val permsDict = doc.documentCatalog.cosObject.getCOSDictionary(COSName.PERMS) ?: return 0
    val sigDict = permsDict.getCOSDictionary(COSName.DOCMDP) ?: return 0
    val refArray = sigDict.getCOSArray(COSName.REFERENCE) ?: return 0
    for (i in 0 until refArray.size()) {
      val base = refArray.getObject(i)
      if (base is COSDictionary && COSName.DOCMDP == base.getDictionaryObject(COSName.TRANSFORM_METHOD)) {
        val transformParams = base.getDictionaryObject(COSName.TRANSFORM_PARAMS)
        if (transformParams is COSDictionary) {
          val p = transformParams.getInt(COSName.P, 2)
          return if (p in 1..3) p else 2
        }
      }
    }
    return 0
  }

  private fun createVisualSignatureTemplate(
    srcDoc: PDDocument,
    pageNum: Int,
    rect: PDRectangle,
  ): InputStream {
    PDDocument().use { doc ->
      val srcPage = srcDoc.getPage(pageNum)
      val page = PDPage(srcPage.mediaBox)
      doc.addPage(page)
      val acroForm = PDAcroForm(doc)
      doc.documentCatalog.acroForm = acroForm
      val signatureField = PDSignatureField(acroForm)
      val widget = signatureField.widgets.first()
      acroForm.fields.add(signatureField)
      acroForm.setSignaturesExist(true)
      acroForm.setAppendOnly(true)
      acroForm.cosObject.isDirect = true
      widget.rectangle = rect
      val stream = PDStream(doc)
      val form = PDFormXObject(stream)
      form.resources = PDResources()
      form.formType = 1
      form.bBox = PDRectangle(rect.width, rect.height)
      val height = rect.height
      val font = loadFont(doc, SIGNATURE_FONT_PATH) ?: throw IllegalStateException("Font not found: $SIGNATURE_FONT_PATH")
      val appearanceStream = PDAppearanceStream(form.cosObject)
      val appearance = PDAppearanceDictionary()
      appearance.cosObject.isDirect = true
      appearance.setNormalAppearance(appearanceStream)
      widget.appearance = appearance
      PDPageContentStream(doc, appearanceStream).use { cs ->
        cs.setNonStrokingColor(Color(251, 247, 255))
        cs.addRect(0f, 0f, rect.width, rect.height)
        cs.fill()
        cs.setStrokingColor(Color(120, 81, 169))
        cs.setLineWidth(1f)
        cs.addRect(0f, 0f, rect.width, rect.height)
        cs.stroke()
        val fontSize = 10f
        // Reverse Hebrew string so that when PDF draws LTR, it displays in correct RTL order
        val line1 = "מסמך זה חתום דיגיטלית".reversed()
        val textWidth = font.getStringWidth(line1) / 1000f * fontSize
        val x = (rect.width - textWidth) / 2f
        val y = height / 2f - 3f // vertically centered baseline
        cs.beginText()
        cs.setFont(font, fontSize)
        cs.setNonStrokingColor(Color(30, 30, 30))
        cs.newLineAtOffset(x, y)
        cs.showText(line1)
        cs.endText()
      }
      val baos = ByteArrayOutputStream()
      doc.save(baos)
      return ByteArrayInputStream(baos.toByteArray())
    }
  }

  private fun loadFont(doc: PDDocument, resourcePath: String): PDFont? =
    javaClass.classLoader.getResourceAsStream(resourcePath)?.use { stream ->
      PDType0Font.load(doc, stream, true)
    }
}
