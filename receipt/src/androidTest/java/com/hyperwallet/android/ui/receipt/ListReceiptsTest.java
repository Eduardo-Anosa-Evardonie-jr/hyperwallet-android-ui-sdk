package com.hyperwallet.android.ui.receipt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.util.EspressoUtils.atPosition;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.receipt.repository.ReceiptRepositoryFactory;
import com.hyperwallet.android.ui.receipt.view.ListReceiptActivity;
import com.hyperwallet.android.util.RecyclerViewCountAssertion;
import com.hyperwallet.android.util.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

@RunWith(AndroidJUnit4.class)
public class ListReceiptsTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<ListReceiptActivity> mActivityTestRule =
            new ActivityTestRule<>(ListReceiptActivity.class, true, false);

    @Before
    public void setup() {
        Hyperwallet.getInstance(new TestAuthenticationProvider());

        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();

        setLocale(Locale.US);
    }

    @After
    public void cleanup() {
        ReceiptRepositoryFactory.clearInstance();
    }

    @Test
    public void testListReceipts_userHasMultipleTransactions() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("June 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("Payment")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("+ 20.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 07, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(1,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText("Payment")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText("+ 25.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText("June 02, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(1, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(2,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText("Card Activation Fee")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText("- 1.95")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText("June 01, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(2, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(3, hasDescendant(withText("December 2018")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText("Card Load")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText("- 18.05")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText("December 01, 2018")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(4));
    }

    @Test
    public void testListReceipts_displayCreditTransaction() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_credit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("June 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.payment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("+ 25.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 02, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListReceipts_displayDebitTransaction() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("May 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.transfer_to_prepaid_card)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("- 18.05")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("May 02, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListReceipts_displayUnknownTransactionType() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_unknown_type_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("June 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.unknown_type)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("+ 25.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 02, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListReceipt_userHasNoTransactions() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        //todo: check empty view when it will be ready
    }

    @Test
    public void testListReceipts_checkDateTextOnLocaleChange() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        setLocale(Locale.ITALY);
        // run test
        mActivityTestRule.launchActivity(null);
        // assert
        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("maggio 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("maggio 02, 2019")))));
        mActivityTestRule.finishActivity();
        setLocale(Locale.US);
        mActivityTestRule.launchActivity(null);
        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("May 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("May 02, 2019")))));
    }

    private void setLocale(Locale locale) {
        Context context = ApplicationProvider.getApplicationContext();
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}