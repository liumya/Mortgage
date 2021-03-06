package com.bruce.open.mortgage.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bruce.open.mortgage.IPayStrategy.EqualCorpusStrategy;
import com.bruce.open.mortgage.IPayStrategy.EqualInterestStrategy;
import com.bruce.open.mortgage.IPayStrategy.PayContext;
import com.bruce.open.mortgage.Model.PayResult;
import com.bruce.open.mortgage.R;
import com.bruce.open.mortgage.Utils.Methods;
import com.bruce.open.mortgage.Utils.SettingManager;
import com.bruce.open.mortgage.adapter.BaseSpinnerAdapter;
import com.bruce.open.mortgage.customViews.CustomScrollView;
import com.bruce.open.mortgage.customViews.CustomTextView;
import com.bruce.open.mortgage.customViews.DesktopTabHost;

import java.text.DecimalFormat;

/**
 * Created by qizhenghao on 16/6/23.
 */
public class MortgageCalculateFragment extends BaseFragment implements View.OnClickListener{

    private ScrollView scrollView;
    private RadioGroup mortgageTypeRG;
    private RadioGroup calculateTypeRG;
    private EditText unitPriceEdit;
    private EditText areaEdit;
    private Spinner firstPaySp;
    private EditText sumLoanEdit;
    private EditText sumBussLoanEdit;
    private EditText sumHousingLoanEdit;

    private Spinner yearSp;
    private Spinner bussRateSp;
    private Spinner housingRateSp;

    private RadioGroup payTypeRG;

    private Button calculateBtn;
    private Button refillBtn;

    private CustomTextView resSumPriceTV;
    private CustomTextView resSumLoanTV;
    private CustomTextView resSumPaymentTV;
    private CustomTextView resSumInterestTV;
    private CustomTextView resfirstPaymentTV;
    private CustomTextView resSumMonthTV;
    private CustomTextView resEveryMonthPaymentTV;

    private float[] firstPayArr = new float[10];
    private float[] yearArr = new float[30];
    private float[] bussRateArr = new float[]{5.25f, 5.15f, 5.00f, 4.90f, 4.80f, 4.70f, 4.60f, 4.50f, 4.40f, 4.30f,
            4.20f, 4.10f, 4.00f};
    private float[] housingRateArr = new float[]{4.00f, 3.90f, 3.80f, 3.70f, 3.60f, 3.50f, 3.40f, 3.30f, 3.25f};

    private BaseSpinnerAdapter firstPayAdapter;
    private BaseSpinnerAdapter yearAdapter;
    private BaseSpinnerAdapter bussRateAdapter;
    private BaseSpinnerAdapter housingRateAdapter;

    PayResult result = null;
    private PayResult housingResult;
    private PayResult bussResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_mortgage_calculate_layout, null);
        return mContentView;
    }

    @Override
    protected void initView() {
        scrollView = (ScrollView) mContentView.findViewById(R.id.mortgage_calculate_sv);
        mortgageTypeRG = (RadioGroup) mContentView.findViewById(R.id.mortgage_type_rg);
        calculateTypeRG = (RadioGroup) mContentView.findViewById(R.id.calculate_type_rg);
        unitPriceEdit = (EditText) mContentView.findViewById(R.id.calculate_unit_price_et);
        areaEdit = (EditText) mContentView.findViewById(R.id.calculate_area_et);
        firstPaySp = (Spinner) mContentView.findViewById(R.id.calculate_unit_price_payment_ratio_spinner);
        sumLoanEdit = (EditText) mContentView.findViewById(R.id.calculate_sum_price_et);
        sumBussLoanEdit = (EditText) mContentView.findViewById(R.id.calculate_type_combine_buss_et);
        sumHousingLoanEdit = (EditText) mContentView.findViewById(R.id.calculate_type_combine_housing_et);
        sumLoanEdit = (EditText) mContentView.findViewById(R.id.calculate_sum_price_et);
        yearSp = (Spinner) mContentView.findViewById(R.id.mortgage_year_spinner);
        bussRateSp = (Spinner) mContentView.findViewById(R.id.mortgage_rate_spinner);
        housingRateSp = (Spinner) mContentView.findViewById(R.id.housing_rate_spinner);
        payTypeRG = (RadioGroup) mContentView.findViewById(R.id.repayment_type_rg);
        calculateBtn = (Button) mContentView.findViewById(R.id.calculate_btn);
        refillBtn = (Button) mContentView.findViewById(R.id.fill_again_btn);
        resSumPriceTV = (CustomTextView) mContentView.findViewById(R.id.result_sum_price_tv);
        resSumLoanTV = (CustomTextView) mContentView.findViewById(R.id.result_sum_loan_edit);
        resSumPaymentTV = (CustomTextView) mContentView.findViewById(R.id.result_repayment_sum_price_edit);
        resSumInterestTV = (CustomTextView) mContentView.findViewById(R.id.result_sum_interest_edit);
        resfirstPaymentTV = (CustomTextView) mContentView.findViewById(R.id.result_fisrt_payment_price_edit);
        resSumMonthTV = (CustomTextView) mContentView.findViewById(R.id.result_sum_month_edit);
        resEveryMonthPaymentTV = (CustomTextView) mContentView.findViewById(R.id.result_every_month_payment_edit);

        mContentView.findViewById(R.id.mortgage_type_tv).setFocusable(true);//设置不自动弹出输入法，也可以在manifest中设置

        initProgressBar((ViewGroup) mActivity.getWindow().getDecorView(), true);
    }

    @Override
    protected void initData() {
        SettingManager settingManager = SettingManager.getInstance();
        mortgageTypeRG.check(settingManager.getLoanType());
        calculateTypeRG.check(settingManager.getCalculateType());
        payTypeRG.check(settingManager.getPayType());

        firstPayAdapter = new BaseSpinnerAdapter(mContext, BaseSpinnerAdapter.FIRST_PAYMENT, firstPayArr);
        yearAdapter = new BaseSpinnerAdapter(mContext, BaseSpinnerAdapter.YEAR, yearArr);
        bussRateAdapter = new BaseSpinnerAdapter(mContext, BaseSpinnerAdapter.BUSSINESS_RATE, bussRateArr);
        housingRateAdapter = new BaseSpinnerAdapter(mContext, BaseSpinnerAdapter.HOUSING_RATE, housingRateArr);

        firstPaySp.setAdapter(firstPayAdapter);
        firstPaySp.setSelection((int) settingManager.getFirstPay());

        yearSp.setAdapter(yearAdapter);
        yearSp.setSelection((int) settingManager.getYear() - 1);

        initSpinnerSelection(bussRateSp, bussRateAdapter, settingManager.getBussRate(), bussRateArr);

        initSpinnerSelection(housingRateSp, housingRateAdapter, settingManager.getHousingRate(), housingRateArr);

        unitPriceEdit.setText(settingManager.getUnitPrice());
        areaEdit.setText(settingManager.getArea());
        sumLoanEdit.setText(settingManager.getSumLoan());
        sumBussLoanEdit.setText(settingManager.getSumBussLoan());
        sumHousingLoanEdit.setText(settingManager.getSumHousingLoan());
    }

    @Override
    protected void initListener() {
        mortgageTypeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.mortgage_type_housing_fund_rb:
                        mContentView.findViewById(R.id.calculate_type_no_combine_layout).setVisibility(View.VISIBLE);
                        mContentView.findViewById(R.id.calculate_type_combine_layout).setVisibility(View.GONE);
                        mContentView.findViewById(R.id.housing_rate_layout).setVisibility(View.VISIBLE);
                        mContentView.findViewById(R.id.buss_rate_layout).setVisibility(View.GONE);
                        mContentView.findViewById(R.id.calculate_type_sum_layout).setVisibility(calculateTypeRG.getCheckedRadioButtonId() == R.id.calculate_type_sum_mortgage_rb ? View.VISIBLE : View.GONE);
                        break;
                    case R.id.mortgage_type_business_rb:
                        mContentView.findViewById(R.id.housing_rate_layout).setVisibility(View.GONE);
                        mContentView.findViewById(R.id.buss_rate_layout).setVisibility(View.VISIBLE);
                        mContentView.findViewById(R.id.calculate_type_no_combine_layout).setVisibility(View.VISIBLE);
                        mContentView.findViewById(R.id.calculate_type_combine_layout).setVisibility(View.GONE);
                        mContentView.findViewById(R.id.calculate_type_sum_layout).setVisibility(calculateTypeRG.getCheckedRadioButtonId() == R.id.calculate_type_sum_mortgage_rb ? View.VISIBLE : View.GONE);
                        break;
                    case R.id.mortgage_type_combine_rb:
                        mContentView.findViewById(R.id.calculate_type_no_combine_layout).setVisibility(View.GONE);
                        mContentView.findViewById(R.id.calculate_type_combine_layout).setVisibility(View.VISIBLE);
                        mContentView.findViewById(R.id.housing_rate_layout).setVisibility(View.VISIBLE);
                        mContentView.findViewById(R.id.buss_rate_layout).setVisibility(View.VISIBLE);
                        mContentView.findViewById(R.id.calculate_type_sum_layout).setVisibility(View.GONE);
                        break;
                }
            }
        });
        calculateTypeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.calculate_type_sum_mortgage_rb) {
                    mContentView.findViewById(R.id.calculate_type_unit_layout).setVisibility(View.GONE);
                    mContentView.findViewById(R.id.calculate_type_sum_layout).setVisibility(View.VISIBLE);
                } else {
                    mContentView.findViewById(R.id.calculate_type_unit_layout).setVisibility(View.VISIBLE);
                    mContentView.findViewById(R.id.calculate_type_sum_layout).setVisibility(View.GONE);
                }
            }
        });
        mContentView.findViewById(R.id.calculate_btn).setOnClickListener(this);
        mContentView.findViewById(R.id.fill_again_btn).setOnClickListener(this);
        mContentView.findViewById(R.id.mortgage_calculate_save_result_btn).setOnClickListener(this);
    }

    @Override
    public void refresh() {

    }

    private void initSpinnerSelection(Spinner spinner, BaseSpinnerAdapter adapter, float selectValue, float[] arr) {
        spinner.setAdapter(adapter);
        for (int i=0;i<arr.length;i++) {
            if (arr[i] == selectValue) {
                spinner.setSelection(i);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calculate_btn:
                showProgressBar();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final PayResult payResult = calculate();
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressBar();
                                if (payResult != null) {
                                    setResultViewData(payResult);
                                    scrollView.fullScroll(View.FOCUS_DOWN);
                                }
                            }
                        });
                    }
                }).start();
                break;
            case R.id.fill_again_btn:
                unitPriceEdit.setText("");
                areaEdit.setText("");
                sumLoanEdit.setText("");
                sumBussLoanEdit.setText("");
                sumHousingLoanEdit.setText("");
                break;
            case R.id.mortgage_calculate_save_result_btn:
                if (result == null)
                    Toast.makeText(mActivity, "需要先计算哦", Toast.LENGTH_SHORT).show();
                else {
                    result.beginTime = System.currentTimeMillis();
                    SettingManager.getInstance().setMyMortgageResultJson(result.toJSONObj().toString());
                    if (bussResult != null && result.loanType.contains("组合"))//贷款类型为组合
                        SettingManager.getInstance().setMyMortgageBussResultJson(bussResult.toJSONObj().toString());
                    if (housingResult != null && result.loanType.contains("组合"))//贷款类型为组合
                        SettingManager.getInstance().setMyMortgageHousingResultJson(housingResult.toJSONObj().toString());
                    onRefreshFragmentListener.onRefresh(MyMortgageFragment.class);
                    onTabItemClickListener.onTabItemSelected(DesktopTabHost.TabType.MY_MORTGAGE, null);
                }
                break;
        }
    }

    @Nullable
    private PayResult calculate() {
        double unitPrice = 0, area = 0, sumLoan = 0, bussRate = 0, housingRate = 0, sumBussLoan = 0, sumHousingLoan = 0;
        if (mortgageTypeRG.getCheckedRadioButtonId() == R.id.mortgage_type_combine_rb) {
            sumBussLoan = getLoanFromEdit(sumBussLoanEdit);
            sumHousingLoan = getLoanFromEdit(sumHousingLoanEdit);
        } else {
            switch (calculateTypeRG.getCheckedRadioButtonId()) {
                case R.id.calculate_type_sum_mortgage_rb:
                    sumLoan = getLoanFromEdit(sumLoanEdit);
                    break;
                case R.id.calculate_type_unit_price_rb:
                    unitPrice = Double.parseDouble("".equals(unitPriceEdit.getText().toString().trim())?"0":unitPriceEdit.getText().toString().trim());
                    area = Double.parseDouble("".equals(areaEdit.getText().toString().trim())?"0": areaEdit.getText().toString().trim());
                    break;
                default:
                    Methods.showToast("请选择计算方式", false);
                    return null;
            }
        }

        double firstPay = firstPaySp.getSelectedItemPosition()/10d;

        int year = yearSp.getSelectedItemPosition()+1;

        switch (payTypeRG.getCheckedRadioButtonId()) {
            case R.id.repayment_type_interest_rb:
                break;
            case R.id.repayment_type_corpus_rb:
                break;
            default:
                Methods.showToast("请选择还款方式", false);
                return null;
        }

        switch (mortgageTypeRG.getCheckedRadioButtonId()) {
            case R.id.mortgage_type_business_rb:
                bussRate = bussRateArr[bussRateSp.getSelectedItemPosition()]/100d;
                PayContext bussContext = new PayContext(payTypeRG.getCheckedRadioButtonId() == R.id.repayment_type_interest_rb ? new EqualInterestStrategy(unitPrice, area, sumLoan, firstPay, year, bussRate) : new EqualCorpusStrategy(unitPrice, area, sumLoan, firstPay, year, bussRate));
                result = bussContext.operate();
                break;
            case R.id.mortgage_type_housing_fund_rb:
                housingRate = housingRateArr[housingRateSp.getSelectedItemPosition()]/100d;
                PayContext housingContext = new PayContext(payTypeRG.getCheckedRadioButtonId() == R.id.repayment_type_interest_rb ? new EqualInterestStrategy(unitPrice, area, sumLoan, firstPay, year, housingRate) : new EqualCorpusStrategy(unitPrice, area, sumLoan, firstPay, year, housingRate));
                result = housingContext.operate();
                break;
            case R.id.mortgage_type_combine_rb:
                bussRate = bussRateArr[bussRateSp.getSelectedItemPosition()]/100d;
                housingRate = housingRateArr[housingRateSp.getSelectedItemPosition()]/100d;
                PayContext combineBussContext = new PayContext(payTypeRG.getCheckedRadioButtonId() == R.id.repayment_type_interest_rb ? new EqualInterestStrategy(unitPrice, area, sumBussLoan, firstPay, year, bussRate) : new EqualCorpusStrategy(unitPrice, area, sumBussLoan, firstPay, year, bussRate));
                PayContext combineHousingContext = new PayContext(payTypeRG.getCheckedRadioButtonId() == R.id.repayment_type_interest_rb ? new EqualInterestStrategy(unitPrice, area, sumHousingLoan, firstPay, year, housingRate) : new EqualCorpusStrategy(unitPrice, area, sumHousingLoan, firstPay, year, housingRate));
                bussResult = combineBussContext.operate();
                housingResult = combineHousingContext.operate();
                result = PayResult.add(bussResult, housingResult);
                break;
            default:
                Methods.showToast("请选择贷款类型", false);
                return null;
        }
        result.loanType = "贷款类型：" + ((RadioButton) mContentView.findViewById(mortgageTypeRG.getCheckedRadioButtonId())).getText().toString();
        result.calculateType = "计算方式：" + ((RadioButton) mContentView.findViewById(calculateTypeRG.getCheckedRadioButtonId())).getText().toString();
        result.payType = "还款方式：" + ((RadioButton) mContentView.findViewById(payTypeRG.getCheckedRadioButtonId())).getText().toString();
        result.housingRate = housingRate * 100d;
        result.bussRate = bussRate * 100d;

        if (bussResult != null) {
            bussResult.loanType = result.loanType;
            bussResult.calculateType = result.calculateType;
            bussResult.payType = result.payType;
        }
        if (housingResult != null) {
            housingResult.loanType = result.loanType;
            housingResult.calculateType = result.calculateType;
            housingResult.payType = result.payType;
        }
        return result;
    }

    private void setResultViewData(PayResult result) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        resSumPriceTV.setText(result.sumPrice == 0 ? "0" : decimalFormat.format(result.sumPrice));
        resSumLoanTV.setText(result.sumLoan == 0 ? "0" : decimalFormat.format(result.sumLoan));
        resSumPaymentTV.setText(result.sumPayPrice == 0 ? "0" : decimalFormat.format(result.sumPayPrice));
        resSumInterestTV.setText(result.sumInterest == 0 ? "0" : decimalFormat.format(result.sumInterest));
        resfirstPaymentTV.setText(result.firstPay == 0 ? "0" : decimalFormat.format(result.firstPay));
        resSumMonthTV.setText(result.monthCount == 0 ? "0" : decimalFormat.format(result.monthCount));
        if (payTypeRG.getCheckedRadioButtonId() == R.id.repayment_type_interest_rb) {
            resEveryMonthPaymentTV.setText(result.everyMonthPay == 0 ? "0" : decimalFormat.format(result.everyMonthPay));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) resEveryMonthPaymentTV.getLayoutParams();
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            resEveryMonthPaymentTV.setLayoutParams(params);
            CustomScrollView.IS_INTERCEPT = false;
        } else {
            CustomScrollView.IS_INTERCEPT = true;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < result.everyMonthPayArr.length; i++) {
                sb.append("第" + (i+1) + "月，" + decimalFormat.format(result.everyMonthPayArr[i]) + "\n");
            }
            resEveryMonthPaymentTV.setText(sb.toString());
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) resEveryMonthPaymentTV.getLayoutParams();
            params.height = Methods.computePixelsWithDensity(150);
            resEveryMonthPaymentTV.setLayoutParams(params);
        }
    }

    private double getLoanFromEdit(EditText edit) {
        double loan = Double.parseDouble("".equals(edit.getText().toString().trim()) ? "0" : edit.getText().toString().trim());
        if (loan < 1) Methods.showToast("贷款总额不能小于1万", false);
        return loan*10000;
    }

    @Override
    public void onPause() {
        SettingManager settingManager = SettingManager.getInstance();

        settingManager.setLoanType(mortgageTypeRG.getCheckedRadioButtonId());
        settingManager.setCalculateType(calculateTypeRG.getCheckedRadioButtonId());
        settingManager.setPayType(payTypeRG.getCheckedRadioButtonId());

        settingManager.setUnitPrice(unitPriceEdit.getText().toString());
        settingManager.setArea(areaEdit.getText().toString());
        settingManager.setSumLoan(sumLoanEdit.getText().toString());
        settingManager.setSumBussLoan(sumBussLoanEdit.getText().toString());
        settingManager.setSumHousingLoan(sumHousingLoanEdit.getText().toString());

        settingManager.setFirstPay(firstPaySp.getSelectedItemPosition());
        settingManager.setYear(yearSp.getSelectedItemPosition() + 1);
        settingManager.setBussRate(bussRateArr[bussRateSp.getSelectedItemPosition()]);
        settingManager.setHousingRate(housingRateArr[housingRateSp.getSelectedItemPosition()]);
        super.onPause();
    }
}
