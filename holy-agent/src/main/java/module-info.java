import org.jspecify.annotations.NullMarked;

@NullMarked
module dev.thihup.holytime.holy.agent {
    requires java.instrument;
    requires static org.jspecify;
}
