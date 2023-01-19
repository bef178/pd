package pd.entity;

import java.util.Objects;

public class Animal {

    /**
     * 域
     */
    public static final String Domain = "Eukaryota";

    /**
     * 界
     */
    public static final String Kingdom = "Animalia";

    /**
     * 门
     */
    public String Phylum;

    /**
     * 纲
     */
    public String Class;

    /**
     * 目
     */
    public String Order;

    /**
     * 科
     */
    public String Family;

    /**
     * 属
     */
    public String Genus;

    /**
     * 种
     */
    public String Species;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof Animal) {
            Animal another = (Animal) o;
            return Objects.equals(Phylum, another.Phylum) && Objects.equals(Class, another.Class)
                    && Objects.equals(Order, another.Order) && Objects.equals(Family, another.Family)
                    && Objects.equals(Genus, another.Genus) && Objects.equals(Species, another.Species);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 31 + Objects.hashCode(Phylum);
        hashCode = hashCode * 31 + Objects.hashCode(Class);
        hashCode = hashCode * 31 + Objects.hashCode(Order);
        hashCode = hashCode * 31 + Objects.hashCode(Family);
        hashCode = hashCode * 31 + Objects.hashCode(Genus);
        hashCode = hashCode * 31 + Objects.hashCode(Species);
        return hashCode;
    }
}
