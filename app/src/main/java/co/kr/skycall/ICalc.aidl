 package co.kr.skycall.aidl;

interface ICalc{
 int Add(in int a, in int b);
 int Mul(in int a, in int b);
 int Sub(in int a, in int b);
 int Div(in int a, in int b);
}