package north.srs.route;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import north.srs.server.Request;

public final class Matchers {

    private Matchers() {
    }

    /**
     * Returns a matcher that evaluates to {@code true} if the provided matcher evaluates to {@code false}.
     */
    public static Matcher not(Matcher matcher) {
        return new NotMatcher(matcher);
    }

    /**
     * Returns a matcher that evaluates to {@code true} if each of its matchers evaluates to {@code true}. The matchers
     * are evaluated in order, and evaluation will be "short-circuited" as soon as a false matcher is found. It
     * defensively copies the iterable passed in, so future changes to it won't alter the behavior of this matcher. If
     * {@code matchers} is empty, the returned matcher will always evaluate to {@code true}.
     */
    public static Matcher and(Iterable<? extends Matcher> matchers) {
        return new AndMatcher(defensiveCopy(matchers));
    }

    /**
     * Returns a matcher that evaluates to {@code true} if each of its matchers evaluates to {@code true}. The matchers
     * are evaluated in order, and evaluation will be "short-circuited" as soon as a false matcher is found. It
     * defensively copies the array passed in, so future changes to it won't alter the behavior of this matcher. If
     * {@code matchers} is empty, the returned matcher will always evaluate to {@code true}.
     */
    public static Matcher and(Matcher... matchers) {
        return new AndMatcher(defensiveCopy(matchers));
    }

    /**
     * Returns a matcher that evaluates to {@code true} if both of its matchers evaluate to {@code true}. The matchers
     * are evaluated in order, and evaluation will be "short-circuited" as soon as a false matcher is found.
     */
    public static Matcher and(Matcher first, Matcher second) {
        return new AndMatcher(Matchers.asList(checkNotNull(first), checkNotNull(second)));
    }

    /**
     * Returns a matcher that evaluates to {@code true} if any one of its matchers evaluates to {@code true}. The
     * matchers are evaluated in order, and evaluation will be "short-circuited" as soon as a true matcher is found. It
     * defensively copies the iterable passed in, so future changes to it won't alter the behavior of this matcher. If
     * {@code matchers} is empty, the returned matcher will always evaluate to {@code true}.
     */
    public static Matcher or(Iterable<? extends Matcher> matchers) {
        return new OrMatcher(defensiveCopy(matchers));
    }

    /**
     * Returns a matcher that evaluates to {@code true} if any one of its matchers evaluates to {@code true}. The
     * matchers are evaluated in order, and evaluation will be "short-circuited" as soon as a true matcher is found. It
     * defensively copies the array passed in, so future changes to it won't alter the behavior of this matcher. If
     * {@code matchers} is empty, the returned matcher will always evaluate to {@code true}.
     */
    public static Matcher or(Matcher... matchers) {
        return new OrMatcher(defensiveCopy(matchers));
    }

    /**
     * Returns a matcher that evaluates to {@code true} if either of its matchers evaluates to {@code true}. The
     * matchers are evaluated in order, and evaluation will be "short-circuited" as soon as a true matcher is found.
     */
    public static Matcher or(Matcher first, Matcher second) {
        return new OrMatcher(Matchers.asList(first, second));
    }

    /**
     * @see Matchers#not(Matcher)
     */
    private static class NotMatcher extends Matcher {

        final Matcher matcher;

        NotMatcher(Matcher matcher) {
            this.matcher = checkNotNull(matcher);
        }

        @Override
        public boolean apply(Request request) {
            return !matcher.apply(request);
        }

        @Override
        public int hashCode() {
            return ~matcher.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Matchers.NotMatcher) {
                Matchers.NotMatcher that = (Matchers.NotMatcher) obj;
                return matcher.equals(that.matcher);
            }
            return false;
        }

        @Override
        public String toString() {
            return "Not(" + matcher.toString() + ")";
        }
    }

    /**
     * @see Matchers#and(Iterable)
     */
    private static class AndMatcher extends Matcher {

        private final List<? extends Matcher> matchers;

        private AndMatcher(List<? extends Matcher> matchers) {
            this.matchers = matchers;
        }

        @Override
        public boolean apply(Request request) {
            for (int i = 0; i < matchers.size(); i++) {
                if (!matchers.get(i).apply(request)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            // add a random number to avoid collisions with OrMatcher
            return matchers.hashCode() + 0x12472c2c;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Matchers.AndMatcher) {
                Matchers.AndMatcher that = (Matchers.AndMatcher) obj;
                return matchers.equals(that.matchers);
            }
            return false;
        }

        @Override
        public String toString() {
            return "And(" + commaJoin(matchers) + ")";
        }
    }

    /**
     * @see Matchers#or(Iterable)
     */
    private static class OrMatcher extends Matcher {

        private final List<? extends Matcher> matchers;

        private OrMatcher(List<? extends Matcher> matchers) {
            this.matchers = matchers;
        }

        @Override
        public boolean apply(Request request) {
            for (int i = 0; i < matchers.size(); i++) {
                if (matchers.get(i).apply(request)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            // add a random number to avoid collisions with AndMatcher
            return matchers.hashCode() + 0x053c91cf;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Matchers.AndMatcher) {
                Matchers.AndMatcher that = (Matchers.AndMatcher) obj;
                return matchers.equals(that.matchers);
            }
            return false;
        }

        @Override
        public String toString() {
            return "Or(" + commaJoin(matchers) + ")";
        }
    }

    private static List<Matcher> asList(Matcher first, Matcher second) {
        return Arrays.<Matcher>asList(first, second);
    }

    private static List<Matcher> defensiveCopy(Matcher... array) {
        return defensiveCopy(Arrays.asList(array));
    }

    private static List<Matcher> defensiveCopy(Iterable<? extends Matcher> iterable) {
        ArrayList<Matcher> list = new ArrayList<>();
        for (Matcher element : iterable) {
            list.add(checkNotNull(element));
        }
        return list;
    }

    /**
     * @throws NullPointerException if {@code matcher} is null
     */
    private static Matcher checkNotNull(Matcher matcher) {
        if (matcher == null) {
            throw new NullPointerException();
        }
        return matcher;
    }

    private static String commaJoin(List<? extends Matcher> matchers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matchers.size(); i++) {
            sb.append(matchers.get(i).toString());
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
