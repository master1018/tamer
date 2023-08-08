public class PriceCalculatorItem {
    private PaymentType paymentType;
    private PriceStrategy priceStrategy;
    public PriceCalculatorItem(PaymentType paymentType, PriceStrategy priceStrategy) {
        this.paymentType = paymentType;
        this.priceStrategy = priceStrategy;
    }
    public PaymentType getPaymentType() {
        return paymentType;
    }
    public PriceStrategy getPriceStrategy() {
        return priceStrategy;
    }
}
