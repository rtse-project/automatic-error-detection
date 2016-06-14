package org.gudy.bouncycastle.asn1.pkcs;

import org.gudy.bouncycastle.asn1.ASN1EncodableVector;
import org.gudy.bouncycastle.asn1.ASN1Sequence;
import org.gudy.bouncycastle.asn1.BERSequence;
import org.gudy.bouncycastle.asn1.DEREncodable;
import org.gudy.bouncycastle.asn1.DERObject;

public class AuthenticatedSafe
    implements DEREncodable
{
    ContentInfo[]    info;

    public AuthenticatedSafe(
        ASN1Sequence  seq)
    {
        info = new ContentInfo[seq.size()];

        for (int i = 0; i != info.length; i++)
        {
            info[i] = ContentInfo.getInstance(seq.getObjectAt(i));
        }
    }

    public AuthenticatedSafe(
        ContentInfo[]       info)
    {
        this.info = info;
    }

    public ContentInfo[] getContentInfo()
    {
        return info;
    }

    public DERObject getDERObject()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        for (int i = 0; i != info.length; i++)
        {
            v.add(info[i]);
        }

        return new BERSequence(v);
    }
}
