package my.help.finance.invest;

public record OFZBondSummary (
     String shortname,
     Integer faceValue,
     Double couponValue,
     String bondTypeDisplay,
     String maturityDate,
     Double price,
     Double yield)
{}