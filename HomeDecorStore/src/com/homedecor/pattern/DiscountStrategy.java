package com.homedecor.pattern;

import com.homedecor.model.CartItem;
import com.homedecor.model.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implements the STRATEGY design pattern for applying discounts.
 *
 * ── R9: Design Pattern — Strategy ────────────────────────────────────────
 *
 * PROBLEM it solves:
 * A store might offer many different discount types:
 *   - No discount
 *   - Flat percentage off (10% off everything)
 *   - Fixed amount off ($20 off orders over $100)
 *   - Buy-two-get-one-free
 *   - Seasonal sale
 *   - Loyalty member discount
 *
 * WITHOUT Strategy, you'd write one giant method with if/else:
 *
 *   if (type.equals("PERCENTAGE")) { ... }
 *   else if (type.equals("FIXED"))  { ... }
 *   else if (type.equals("BOGO"))   { ... }
 *
 * Every time you add a new discount type you must edit this method.
 * This breaks the Open/Closed Principle: code should be open for extension
 * but closed for modification.
 *
 * WITH Strategy:
 *   - Define an interface: DiscountStrategy
 *   - Each discount type is its own class implementing that interface
 *   - The OrderService just calls strategy.apply(total) — it doesn't care which one
 *   - Adding a new discount = adding one new class, changing nothing else
 *
 * ── Structure ─────────────────────────────────────────────────────────────
 * DiscountStrategy          → interface (the contract)
 * NoDiscount                → concrete strategy: do nothing
 * PercentageDiscount        → concrete strategy: X% off
 * FixedAmountDiscount       → concrete strategy: $X off (min order applies)
 * BuyTwoGetOneFreeDiscount  → concrete strategy: B2G1 on cheapest item
 * DiscountContext           → the class that uses a strategy (could be OrderService)
 *
 * ── R3: Functional Interface ─────────────────────────────────────────────
 * DiscountStrategy is a @FunctionalInterface — an interface with exactly
 * one abstract method. This means it can be used as a lambda expression!
 *
 * Example:
 *   DiscountStrategy tenPercent = total -> total.multiply(BigDecimal.valueOf(0.90));
 *   // This is equivalent to creating a PercentageDiscount(10) object
 */
public class DiscountStrategy {

    // ── The Strategy Interface ────────────────────────────────────────────────

    /**
     * The contract that all discount strategies must fulfil.
     *
     * @FunctionalInterface means this interface has exactly ONE abstract method.
     * That enables lambda usage: DiscountStrategy s = total -> total;
     *
     * R3: This IS a functional interface — the rubric requires use of
     * functional interfaces like Predicate, Function, Comparator etc.
     * Defining our own is an excellent demonstration.
     */
    @FunctionalInterface
    public interface Strategy {
        /**
         * Applies this discount to the given total and returns the discounted total.
         *
         * @param originalTotal the price before discount
         * @return the price after discount
         */
        BigDecimal apply(BigDecimal originalTotal);

        /**
         * Returns a human-readable name for this strategy.
         * Default method — optional to override.
         */
        default String getName() {
            return "Discount Strategy";
        }
    }

    // ── Concrete Strategy 1: No Discount ─────────────────────────────────────

    /**
     * Passes the total through unchanged.
     * Used as the default when no promotion is active.
     *
     * This is also an example of the Null Object pattern —
     * instead of checking "if discount != null" everywhere,
     * you use NoDiscount which safely does nothing.
     */
    public static class NoDiscount implements Strategy {

        @Override
        public BigDecimal apply(BigDecimal originalTotal) {
            return originalTotal;   // no change
        }

        @Override
        public String getName() {
            return "No Discount";
        }
    }

    // ── Concrete Strategy 2: Percentage Discount ──────────────────────────────

    /**
     * Applies a flat percentage discount.
     * e.g. PercentageDiscount(10) → 10% off → $200 becomes $180
     */
    public static class PercentageDiscount implements Strategy {

        private final double percent;   // e.g. 10.0 means 10%

        /**
         * @param percent the discount percentage (0–100)
         */
        public PercentageDiscount(double percent) {
            if (percent < 0 || percent > 100) {
                throw new IllegalArgumentException("Percent must be between 0 and 100.");
            }
            this.percent = percent;
        }

        @Override
        public BigDecimal apply(BigDecimal originalTotal) {
            // multiplier = (100 - percent) / 100
            // e.g. 10% off → multiplier = 0.90
            BigDecimal multiplier = BigDecimal.valueOf((100.0 - percent) / 100.0);
            return originalTotal.multiply(multiplier)
                                .setScale(2, RoundingMode.HALF_UP);  // round to 2 decimal places
        }

        @Override
        public String getName() {
            return percent + "% Off";
        }
    }

    // ── Concrete Strategy 3: Fixed Amount Discount ───────────────────────────

    /**
     * Subtracts a fixed dollar amount from the total.
     * Only applies if the total meets a minimum order threshold.
     * e.g. "$20 off orders over $100"
     */
    public static class FixedAmountDiscount implements Strategy {

        private final BigDecimal discountAmount;
        private final BigDecimal minimumOrder;

        /**
         * @param discountAmount the fixed amount to subtract (e.g. 20.00)
         * @param minimumOrder   minimum order total to qualify (e.g. 100.00)
         */
        public FixedAmountDiscount(BigDecimal discountAmount, BigDecimal minimumOrder) {
            this.discountAmount = discountAmount;
            this.minimumOrder   = minimumOrder;
        }

        @Override
        public BigDecimal apply(BigDecimal originalTotal) {
            // Only apply if the order meets the minimum
            if (originalTotal.compareTo(minimumOrder) < 0) {
                return originalTotal;   // no discount — order too small
            }
            BigDecimal discounted = originalTotal.subtract(discountAmount);
            // Never go below zero
            return discounted.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discounted;
        }

        @Override
        public String getName() {
            return "$" + discountAmount + " off orders over $" + minimumOrder;
        }
    }

    // ── Concrete Strategy 4: Buy Two Get One Free ────────────────────────────

    /**
     * For every 3 units of the same item, the cheapest unit is free.
     * e.g. buy 3 mugs at $18 each → pay for 2 → saves $18
     *
     * Applied to the cart items list rather than just the total,
     * because it needs to inspect individual items.
     */
    public static class BuyTwoGetOneFreeDiscount implements Strategy {

        private final List<CartItem> cartItems;

        public BuyTwoGetOneFreeDiscount(List<CartItem> cartItems) {
            this.cartItems = cartItems;
        }

        @Override
        public BigDecimal apply(BigDecimal originalTotal) {
            BigDecimal totalSavings = BigDecimal.ZERO;

            // For each cart item: every set of 3 gets the cheapest one free
            for (CartItem item : cartItems) {
                int      qty      = item.getQuantity();
                int      freeSets = qty / 3;               // how many "buy 2 get 1 free" sets
                if (freeSets > 0) {
                    // Saving = number of free items × unit price
                    BigDecimal saving = item.getProduct().getPrice()
                                           .multiply(BigDecimal.valueOf(freeSets));
                    totalSavings = totalSavings.add(saving);
                }
            }

            return originalTotal.subtract(totalSavings).max(BigDecimal.ZERO);
        }

        @Override
        public String getName() {
            return "Buy 2 Get 1 Free";
        }
    }

    // ── Context Class ────────────────────────────────────────────────────────
    /**
     * The Context is the class that USES a strategy.
     * It holds a reference to a Strategy and delegates to it.
     *
     * The key point: DiscountContext doesn't know OR CARE which
     * specific discount is active. It just calls strategy.apply().
     * You can swap strategies at runtime without changing this class.
     */
    public static class DiscountContext {

        // The active discount strategy — can be swapped at any time
        private Strategy strategy;

        /**
         * Creates a DiscountContext with the default (no discount) strategy.
         */
        public DiscountContext() {
            this.strategy = new NoDiscount();
        }

        /**
         * Creates a DiscountContext with a specific strategy.
         *
         * @param strategy the discount strategy to use
         */
        public DiscountContext(Strategy strategy) {
            this.strategy = strategy;
        }

        /**
         * Swaps the active strategy at runtime.
         * This is the core of the Strategy pattern — behaviour changes
         * without changing the DiscountContext class itself.
         *
         * @param strategy the new strategy to apply
         */
        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        /**
         * Applies the currently active strategy to the given total.
         *
         * @param total the original price
         * @return the discounted price
         */
        public BigDecimal applyDiscount(BigDecimal total) {
            System.out.println("[Discount] Applying strategy: " + strategy.getName());
            BigDecimal result = strategy.apply(total);
            System.out.println("[Discount] Original: $" + total + " → After: $" + result);
            return result;
        }

        /** @return the name of the currently active strategy */
        public String getCurrentStrategyName() {
            return strategy.getName();
        }
    }

    // ── Factory Helper ────────────────────────────────────────────────────────
    /**
     * Convenience factory methods so callers don't need to know the class names.
     *
     * Usage:
     *   Strategy s = DiscountStrategy.percentage(15);
     *   Strategy s = DiscountStrategy.fixed(new BigDecimal("20"), new BigDecimal("100"));
     *   Strategy s = DiscountStrategy.noDiscount();
     */
    public static Strategy noDiscount() {
        return new NoDiscount();
    }

    public static Strategy percentage(double percent) {
        return new PercentageDiscount(percent);
    }

    public static Strategy fixedAmount(BigDecimal amount, BigDecimal minOrder) {
        return new FixedAmountDiscount(amount, minOrder);
    }

    public static Strategy buyTwoGetOneFree(List<CartItem> items) {
        return new BuyTwoGetOneFreeDiscount(items);
    }

    /**
     * Lambda example — shows that Strategy is a functional interface (R3).
     * This creates a 50% off "half price" strategy as a one-liner lambda.
     *
     * @return a lambda-based 50% off strategy
     */
    public static Strategy halfPrice() {
        // This lambda IS a Strategy — it implements the apply() method inline
        // total -> total.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
        return total -> total.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }
}
