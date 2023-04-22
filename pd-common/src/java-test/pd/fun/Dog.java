package pd.fun;

import java.util.Objects;

public class Dog extends Animal {

    public String name;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof Dog) {
            Dog another = (Dog) o;
            return super.equals(another) && Objects.equals(name, another.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode = hashCode * 31 + Objects.hashCode(name);
        return hashCode;
    }
}
