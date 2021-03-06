//Generated by FAMILIAR
CPTerminal : Connectivity+ [Identification] PaymentSchema+ CardReader+ :: _CPTerminal ;

Connectivity : Online+ :: Online_
	| Offline ;

Online : PSTN
	| Mobile3G
	| PrivateWAN
	| VPN ;

Identification : Signature [PIN] :: _Identification ;

PaymentSchema : DirectDebit+ :: DirectDebit_
	| CreditCard ;

DirectDebit : DebitCard
	| PrepaidCard
	| EPurse ;

CardReader : Chip
	| NFC
	| MagStrip ;

%%

not PrepaidCard or Chip or NFC ;
not EPurse or Chip or NFC ;
CreditCard implies Identification ;
DebitCard implies PIN ;


