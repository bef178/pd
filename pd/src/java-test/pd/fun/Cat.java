package pd.fun;

import java.time.Instant;
import java.util.Objects;

public class Cat extends Animal {

    public String name;
    public Instant birthTime;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof Cat) {
            Cat another = (Cat) o;
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
