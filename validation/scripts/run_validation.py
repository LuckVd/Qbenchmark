#!/usr/bin/env python3
"""
Vulnerability Validation Script

This script validates that all vulnerabilities in the benchmark are actually exploitable.
It tests each endpoint with the provided payloads and checks if the expected behavior occurs.
"""

import json
import requests
import sys
import time
import argparse
from typing import Dict, List, Optional
from urllib.parse import urlencode

# Configuration
BASE_URL = "http://localhost:8080"
TIMEOUT = 10
TEST_RESULTS_FILE = "validation/test_results.json"

class VulnerabilityValidator:
    def __init__(self, base_url: str = BASE_URL):
        self.base_url = base_url
        self.results = []
        self.passed = 0
        self.failed = 0
        self.skipped = 0

    def load_test_cases(self, test_file: str = "validation/test-cases.json") -> List[Dict]:
        """Load test cases from JSON file"""
        try:
            with open(test_file, 'r') as f:
                data = json.load(f)
                return data.get('test_cases', [])
        except FileNotFoundError:
            print(f"Error: Test case file {test_file} not found")
            return []

    def send_request(self, test_case: Dict) -> requests.Response:
        """Send HTTP request based on test case configuration"""
        url = self.base_url + test_case['endpoint']

        headers = {}
        if test_case.get('content_type'):
            headers['Content-Type'] = test_case['content_type']

        # Prepare request based on method
        if test_case['method'].upper() == 'GET':
            # Parse payload into query parameters
            if '?' in test_case['payload']:
                url += '?' + test_case['payload']
            else:
                # Parse key=value format
                params = dict(param.split('=') for param in test_case['payload'].split('&'))
                url += '?' + urlencode(params)
            response = requests.get(url, headers=headers, timeout=TIMEOUT)
        else:  # POST
            headers['Content-Type'] = test_case.get('content_type', 'application/json')
            response = requests.post(url, data=test_case['payload'], headers=headers, timeout=TIMEOUT)

        return response

    def check_indicators(self, response: requests.Response, test_case: Dict) -> bool:
        """Check if response contains expected success indicators"""
        success_indicators = test_case.get('success_indicators', [])

        if not success_indicators:
            # If no indicators defined, check if response is successful
            return response.status_code == 200

        response_text = response.text

        for indicator in success_indicators:
            if indicator.lower() in response_text.lower():
                return True

        return False

    def validate_test_case(self, test_case: Dict) -> Dict:
        """Validate a single test case"""
        result = {
            'id': test_case['id'],
            'name': test_case['name'],
            'category': test_case['category'],
            'passed': False,
            'status': 'FAILED',
            'message': '',
            'response_code': None,
            'response_preview': ''
        }

        try:
            response = self.send_request(test_case)
            result['response_code'] = response.status_code
            result['response_preview'] = response.text[:200]

            # Check for success indicators
            if self.check_indicators(response, test_case):
                result['passed'] = True
                result['status'] = 'PASSED'
                result['message'] = 'Vulnerability confirmed exploitable'
            else:
                result['status'] = 'FAILED'
                result['message'] = 'Expected indicators not found in response'

        except requests.exceptions.ConnectionError:
            result['status'] = 'SKIPPED'
            result['message'] = 'Connection failed - is the server running?'
        except requests.exceptions.Timeout:
            result['status'] = 'FAILED'
            result['message'] = 'Request timeout'
        except Exception as e:
            result['status'] = 'FAILED'
            result['message'] = f'Error: {str(e)}'

        return result

    def run_all_tests(self, category: Optional[str] = None, test_id: Optional[str] = None):
        """Run validation tests"""
        test_cases = self.load_test_cases()

        # Filter by category or ID if specified
        if category:
            test_cases = [tc for tc in test_cases if tc['category'] == category]
        if test_id:
            test_cases = [tc for tc in test_cases if tc['id'] == test_id]

        if not test_cases:
            print("No test cases found matching criteria")
            return

        print(f"\n{'='*60}")
        print(f"Running {len(test_cases)} validation test(s) against {self.base_url}")
        print(f"{'='*60}\n")

        for i, test_case in enumerate(test_cases, 1):
            print(f"[{i}/{len(test_cases)}] Testing: {test_case['id']} - {test_case['name']}")
            print(f"  Category: {test_case['category']}")
            print(f"  Endpoint: {test_case['method']} {test_case['endpoint']}")
            print(f"  Payload: {test_case['payload'][:80]}...")

            result = self.validate_test_case(test_case)
            self.results.append(result)

            if result['status'] == 'PASSED':
                self.passed += 1
                print(f"  ✓ PASSED: {result['message']}")
            elif result['status'] == 'SKIPPED':
                self.skipped += 1
                print(f"  ⊘ SKIPPED: {result['message']}")
            else:
                self.failed += 1
                print(f"  ✗ FAILED: {result['message']}")
                print(f"    Response: {result['response_preview']}")

            print()

        self.print_summary()
        self.save_results()

    def print_summary(self):
        """Print test summary"""
        total = self.passed + self.failed + self.skipped
        print(f"\n{'='*60}")
        print("VALIDATION SUMMARY")
        print(f"{'='*60}")
        print(f"Total Tests:  {total}")
        print(f"PASSED:       {self.passed} ({self.passed*100//total if total > 0 else 0}%)")
        print(f"FAILED:       {self.failed} ({self.failed*100//total if total > 0 else 0}%)")
        print(f"SKIPPED:      {self.skipped}")

        if self.passed == total - self.skipped:
            print("\n✓ All vulnerabilities verified exploitable!")
        else:
            print(f"\n✗ {self.failed} vulnerabilities failed validation")

        print(f"{'='*60}\n")

    def save_results(self):
        """Save test results to JSON file"""
        output = {
            'timestamp': time.strftime('%Y-%m-%d %H:%M:%S'),
            'base_url': self.base_url,
            'summary': {
                'total': len(self.results),
                'passed': self.passed,
                'failed': self.failed,
                'skipped': self.skipped
            },
            'results': self.results
        }

        with open(TEST_RESULTS_FILE, 'w') as f:
            json.dump(output, f, indent=2)

        print(f"Results saved to {TEST_RESULTS_FILE}")


def main():
    parser = argparse.ArgumentParser(description='Vulnerability Validation Script')
    parser.add_argument('--url', default=BASE_URL, help='Target base URL')
    parser.add_argument('--category', help='Filter by vulnerability category')
    parser.add_argument('--id', help='Filter by test ID')
    parser.add_argument('--output', help='Output file for results')

    args = parser.parse_args()

    validator = VulnerabilityValidator(base_url=args.url)
    validator.run_all_tests(category=args.category, test_id=args.id)


if __name__ == '__main__':
    main()
