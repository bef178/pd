package junit;

import java.util.List;

import libcliff.io.Codec;
import libcliff.io.InstallmentByteBuffer;
import libcliff.io.Nextable;
import org.junit.Assert;
import org.junit.Test;

public class TestCodecCsv {

    @Test
    public void test1() {
        Nextable it = new InstallmentByteBuffer().append(
                "SetNumber,PartID,Quantity,Colour").reader();
        List<String> l = Codec.Csv.decode(it);
        Assert.assertEquals("SetNumber", l.get(0));
        Assert.assertEquals("PartID", l.get(1));
        Assert.assertEquals("Quantity", l.get(2));
        Assert.assertEquals("Colour", l.get(3));
    }

    @Test
    public void test2() {
        Nextable it = new InstallmentByteBuffer().append(
                "\"8285-1\",4297719,2,'Black'").reader();
        List<String> l = Codec.Csv.decode(it);
        Assert.assertEquals("8285-1", l.get(0));
        Assert.assertEquals("4297719", l.get(1));
        Assert.assertEquals("2", l.get(2));
        Assert.assertEquals("Black", l.get(3));
    }
}
